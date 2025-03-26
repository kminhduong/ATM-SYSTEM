package com.atm.controller;

import com.atm.dto.AccountDTO;
import com.atm.dto.LoginRequest;
import com.atm.model.Account;
import com.atm.service.AccountService;
import com.atm.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Autowired
    private JwtUtil jwtUtil;

    // Đăng ký tài khoản mới (tự động tạo user nếu chưa có)
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AccountDTO accountDTO) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn cần đăng nhập trước!");
        }

        String token = authHeader.substring(7);
        String accountNumber = jwtUtil.validateToken(token);

        if (accountNumber == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ!");
        }

        // Kiểm tra vai trò
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền thực hiện hành động này!");
        }

        try {
            String userId = accountDTO.getUserId();

            if (!accountService.isUserExists(userId)) {
                userId = accountService.createUser(accountDTO.getFullName());
            }

            accountDTO.setUserId(userId);
            accountService.register(accountDTO.toAccount());

            return ResponseEntity.ok("Tài khoản đã được đăng ký thành công!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> payload) {
        String accountNumber = payload.get("accountNumber");
        String password = payload.get("password");

        if (accountService.authenticate(accountNumber, password)) {
            // Giả sử bạn có thể lấy vai trò từ cơ sở dữ liệu hoặc có sẵn giá trị vai trò
            String role = "USER"; // Hoặc "ADMIN" nếu tài khoản là admin

            // Tạo token JWT sau khi xác thực thành công
            String token = jwtUtil.generateToken(accountNumber, role);

            // Trả về phản hồi chứa token
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công!");
            response.put("token", token);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tài khoản hoặc mật khẩu."));
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
}