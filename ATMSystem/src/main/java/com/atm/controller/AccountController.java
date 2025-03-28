package com.atm.controller;

import com.atm.dto.AccountDTO;
import com.atm.model.Account;
import com.atm.repository.UserRepository;
import com.atm.service.AccountService;
import com.atm.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final UserRepository userRepository; // Thêm UserRepository

    private final JwtUtil jwtUtil;

    @Autowired
    public AccountController(JwtUtil jwtUtil, AccountService accountService, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
        this.userRepository = userRepository; // ✅ Đã inject UserRepository đúng cách
    }

    // Đăng ký tài khoản mới (tự động tạo user nếu chưa có)
//    @PostMapping("/register")
//    public ResponseEntity<String> register(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestBody AccountDTO accountDTO) {
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập trước!");
//        }
//
//        String token = authHeader.substring(7);
//        String accountNumber = jwtUtil.validateToken(token);
//
//        if (accountNumber == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ!");
//        }
//
//        // Kiểm tra vai trò
//        String role = jwtUtil.extractRole(token);
//        if (!"ADMIN".equals(role)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền thực hiện hành động này!");
//        }
//
//        try {
//            String userId = accountDTO.getUserId();
//
//            if (!accountService.isUserExists(userId)) {
//                userId = accountService.createUser(accountDTO.getFullName());
//            }
//
//            accountDTO.setUserId(userId);
//            accountService.register(accountDTO.toAccount());
//
//            return ResponseEntity.ok("Tài khoản đã được đăng ký thành công!");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AccountDTO accountDTO) {
        try {
            logger.info("Skipping authorization check for testing.");

            // Kiểm tra nếu userId chưa tồn tại, tự động tạo user
            String userId = accountDTO.getUserId();
            if (userId == null || userId.isEmpty() || !accountService.isUserExists(userId)) {
                userId = accountService.createUser(accountDTO.getFullName());
            }

            accountDTO.setUserId(userId);

            // Đăng ký tài khoản (truyền UserRepository vào toAccount)
            accountService.register(accountDTO.toAccount(userRepository));

            return ResponseEntity.ok("Tài khoản đã được đăng ký thành công!");
        } catch (IllegalArgumentException e) {
            logger.error("Error while registering account: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi!");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> payload) {
        String accountNumber = payload.get("accountNumber");
        String password = payload.get("password");

        // Kiểm tra tài khoản từ database
        Optional<Account> accountOpt = accountService.getAccountByNumberAndPassword(accountNumber, password);

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            String role = account.getRole(); // Lấy role từ database
            String token = jwtUtil.generateToken(account.getAccountNumber(), role, 86400000);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công!");
            response.put("token", token);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tài khoản hoặc mật khẩu."));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateAccount(@RequestBody AccountDTO accountDTO,
                                                @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập trước!");
        }

        String token = authHeader.substring(7);
        String accountNumber = jwtUtil.validateToken(token); // Lấy accountNumber từ token

        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ!");
        }

        try {
            accountService.updateAccount(accountDTO, accountNumber);
            return ResponseEntity.ok("Cập nhật tài khoản thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi cập nhật tài khoản.");
        }
    }

    // Xem toàn bộ khách hàng (dành cho nhân viên ngân hàng)
    @GetMapping("/customers")
    public ResponseEntity<List<AccountDTO>> getAllCustomers(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String token = authHeader.substring(7); // Loại bỏ tiền tố "Bearer "
        String accountNumber = jwtUtil.validateToken(token);

        if (accountNumber != null) {
            List<AccountDTO> customers = accountService.getAllCustomers().stream()
                    .map(AccountDTO::fromAccount)
                    .toList();
            return ResponseEntity.ok(customers);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

//    @GetMapping("/{accountNumber}/balance")
//    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
//        Double balance = accountService.getBalance(accountNumber);
//        if (balance == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
//        }
//        return ResponseEntity.ok(balance);
//    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance() {
        try {
            String loggedInAccountNumber = accountService.getLoggedInAccountNumber();
            System.out.println("🔹 Logged in Account: " + loggedInAccountNumber);

            if (loggedInAccountNumber == null) {
                System.out.println("❌ Authentication failed! SecurityContextHolder is NULL.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            Double balance = accountService.getBalance(loggedInAccountNumber);
            System.out.println("✅ Balance Retrieved: " + balance);

            return ResponseEntity.ok(balance);
        } catch (SecurityException e) {
            System.out.println("❌ SecurityException: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập trước!");
        }

        String token = authHeader.substring(7);
        if (jwtUtil.isTokenValid(token)) {
            // Khi đăng xuất, tạo một token mới với thời gian hết hạn cực ngắn (1ms)
            jwtUtil.generateToken(jwtUtil.validateToken(token), "USER", 1);
        }

        return ResponseEntity.ok("Đăng xuất thành công!");
    }
    @GetMapping("/check-role")
    public ResponseEntity<String> checkRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("👤 User: " + authentication.getName());
        System.out.println("🔐 Authorities: " + authentication.getAuthorities());

        return ResponseEntity.ok("Check console for role details.");
    }

}