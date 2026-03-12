package com.tg.crowdfunding.service;

import com.tg.crowdfunding.dto.request.LoginRequest;
import com.tg.crowdfunding.dto.request.RegisterRequest;
import com.tg.crowdfunding.dto.response.AuthResponse;
import com.tg.crowdfunding.entity.User;
import com.tg.crowdfunding.exception.EmailAlreadyExistsException;
import com.tg.crowdfunding.repository.UserRepository;
import com.tg.crowdfunding.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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
        if (request.getNom() == null || request.getNom().trim().isEmpty() ||
            request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getMotDePasse() == null || request.getMotDePasse().trim().isEmpty()) {
            throw new IllegalArgumentException("Veuillez remplir tous les champs obligatoires.");
        }

        if (request.getMotDePasse().length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caract\u00e8res.");
        }

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
                .orElseThrow();

        String token = jwtUtils.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }
}