package com.arsh.splitwise.auth.service;

import com.arsh.splitwise.auth.dto.AuthResponse;
import com.arsh.splitwise.auth.dto.RegisterRequest;
import com.arsh.splitwise.common.exception.UserAlreadyExistsException;
import com.arsh.splitwise.user.entity.User;
import com.arsh.splitwise.user.entity.UserStatus;
import com.arsh.splitwise.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.arsh.splitwise.auth.dto.LoginRequest;
import com.arsh.splitwise.auth.jwt.JwtService;
import com.arsh.splitwise.auth.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("Email already exists");
        }
        User user= User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
        return AuthResponse.builder()
                .message("User registered successfully")
                .build();
    }
    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .message("Login successful")
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

}
