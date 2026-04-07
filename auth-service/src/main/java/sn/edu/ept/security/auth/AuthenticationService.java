package sn.edu.ept.security.auth;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.edu.ept.security.config.JwtService;
import sn.edu.ept.security.dtos.AuthenticationRequest;
import sn.edu.ept.security.dtos.AuthenticationResponse;
import sn.edu.ept.security.dtos.RegisterRequest;
import sn.edu.ept.security.exception.EmailAlreadyExistsException;
import sn.edu.ept.security.user.Role;
import sn.edu.ept.security.user.User;
import sn.edu.ept.security.user.UserRegisteredEvent;
import sn.edu.ept.security.user.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthEventPublisher authEventPublisher;


    // save credentials in auth_db
    // publish kafka event -> user-service
    // return token
    public AuthenticationResponse register(RegisterRequest request) {

        if (userRepository.existsByEmailAndRole(request.getEmail(), request.getRole())) {
            throw new EmailAlreadyExistsException("Un compte avec cet email et ce rôle existe déjà");
        }
        
        var user = User.builder()
                .email(request.email)
                .password(passwordEncoder.encode(request.password))
                .role(request.role != null ? request.getRole() : Role.CLIENT)
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

        return AuthenticationResponse.builder()
                .message("Compte créé avec succès")
                .accessToken(jwtService.generateToken(savedUser))
                .refreshToken(jwtService.generateRefreshToken(savedUser))
                .role(savedUser.getRole().name())
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
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .role(user.getRole().name())
                .authId(user.getId())
                .build();
    }

    public @Nullable List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
