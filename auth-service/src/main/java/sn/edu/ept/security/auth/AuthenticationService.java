package sn.edu.ept.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.edu.ept.security.config.JwtService;
import sn.edu.ept.security.exception.EmailAlreadyExistsException;
import sn.edu.ept.security.user.Role;
import sn.edu.ept.security.user.User;
import sn.edu.ept.security.user.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    // create a user, encode password, save user and return token
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndRole(request.getEmail(), request.getRole())) {
            throw new EmailAlreadyExistsException("Un compte avec cet email et ce rôle existe déjà");
        }
        
        var user = User.builder()
                .firstname(request.getFirstname() != null ? request.getFirstname() : "")
                .lastname(request.getLastname() != null ? request.getLastname() : "")
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.CLIENT)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .message("Compte créé avec succès")
                .token(jwtToken)
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

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .message("Connexion réussie")
                .token(jwtToken)
                .build();
    }
}
