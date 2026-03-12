package com.tg.crowdfunding.service;

import com.tg.crowdfunding.dto.request.LoginRequest;
import com.tg.crowdfunding.dto.request.RegisterRequest;
import com.tg.crowdfunding.dto.response.AuthResponse;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.exception.EmailAlreadyExistsException;
import com.tg.crowdfunding.repository.UserRepository;
import com.tg.crowdfunding.security.JwtUtils;
import com.tg.crowdfunding.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Normalize phone
        if (request.getTelephone() != null) {
            request.setTelephone(request.getTelephone().replaceAll("\\s+", ""));
        }

        // Require phone for porteur and contributeur
        if (request.getRole() != Role.ADMIN) {
            if (request.getTelephone() == null || request.getTelephone().isBlank()) {
                throw new IllegalArgumentException(
                    "Le numéro de téléphone est obligatoire pour ce type de compte."
                );
            }
        }

        // Block self-registration as admin
        if (request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Rôle non autorisé à l'inscription.");
        }
        
        // Bean validation handles the basic null/size checks now, 
        // but we keep the email existence check below.

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Un compte avec cet email existe d\u00e9j\u00e0.");
        }

        User user = User.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .role(request.getRole())
                .actif(true)
                .banni(false)
                .telephone(request.getTelephone())
                .build();

        userRepository.save(user);

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Identifiants incorrects"));

        if (!passwordEncoder.matches(request.getMotDePasse(), user.getMotDePasse())) {
            throw new BadCredentialsException("Identifiants incorrects");
        }

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }
}