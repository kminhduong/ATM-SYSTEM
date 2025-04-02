package com.atm.controller;

import com.atm.dto.ChangePinRequest;
import com.atm.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credential")
public class CredentialController {

    private final CredentialService credentialService;

    @Autowired
    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping("/change-pin")
    public ResponseEntity<String> changePin(@RequestBody ChangePinRequest request) {
        try {
            // Gọi hàm changePIN trong service
            credentialService.changePIN(request.getOldPin(), request.getNewPin(), request.getConfirmNewPin());
            return ResponseEntity.ok("Mã PIN đã được thay đổi thành công.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
