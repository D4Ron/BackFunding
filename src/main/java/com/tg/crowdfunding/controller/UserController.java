package com.tg.crowdfunding.controller;

import com.tg.crowdfunding.dto.request.UpdateProfileRequest;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<User> getMe(@AuthenticationPrincipal User u) {
        return ResponseEntity.ok(u);
    }

    @PatchMapping("/me")
    public ResponseEntity<User> updateMe(
            @AuthenticationPrincipal User u,
            @RequestBody UpdateProfileRequest req) {
        if (req.getNom() != null) u.setNom(req.getNom());
        if (req.getTelephone() != null) u.setTelephone(req.getTelephone());
        return ResponseEntity.ok(userRepository.save(u));
    }
}
