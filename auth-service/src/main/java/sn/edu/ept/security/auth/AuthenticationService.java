package sn.edu.ept.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.edu.ept.security.client.UserServiceClient;
import sn.edu.ept.security.config.JwtService;
import sn.edu.ept.security.dtos.*;
import sn.edu.ept.security.event.AuthEventPublisher;
import sn.edu.ept.security.event.UserRegisteredEvent;
import sn.edu.ept.security.exception.EmailAlreadyExistsException;
import sn.edu.ept.security.user.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthEventPublisher authEventPublisher;
    private final UserServiceClient userServiceClient;


    // save credentials in auth_db
    // publish kafka event -> user-service
    // return token
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Un compte avec cet email");
        }

        var user = User.builder()
                .email(request.email)
                .password(passwordEncoder.encode(request.password))
                .role(request.role != null ? request.getRole() : Role.CLIENT)
                .firstname(request.firstname)
                .lastname(request.lastname)
                .build();
        User savedUser = userRepository.save(user);

        // Publish event vers kafka
        authEventPublisher.publishUserRegistered(new UserRegisteredEvent(
                savedUser.getId(),
                request.firstname,
                request.lastname,
                request.email,
                request.phone,
                savedUser.getRole().name()
        ));

        return RegisterResponse.builder()
                .message("Compte créé avec succès")
                .authId(savedUser.getId())
                .build();
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        return AuthenticationResponse.builder()
                .message("Connexion réussie")
                .accessToken(jwtService.generateTokenWithClaims(user, user.getId(), user.getRole().name()))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }


    public void changePassword(String email, ChangePasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les nouveaux mots de passe ne correspondent pas");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", email);
    }

}
