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
import sn.edu.ept.security.exception.EmailAlreadyExistsException;
import sn.edu.ept.security.user.Role;
import sn.edu.ept.security.user.User;
import sn.edu.ept.security.user.UserRegisteredEvent;
import sn.edu.ept.security.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    
    // Suivi des utilisateurs connectés
    private final Map<String, LocalDateTime> connectedUsers = new ConcurrentHashMap<>();


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

    public List<UserDTO> getAllUsers() {

        // recuperer les credentials depuis auth_db
        List<User> authUsers = userRepository.findAll();

        if (authUsers.isEmpty()) {
            return List.of();
        }

        // extraire tous les authIds
        List<Long> authIds = authUsers.stream()
                .map(User::getId)
                .toList();

        List<UserProfilDTO> profils = List.of();
        try {
            profils = userServiceClient.getProfilsByAuthIds(authIds);
        } catch (Exception e) {
            log.warn("[AUTH] user-service indisponible, réponse partielle : {}",
                    e.getMessage());
        }

        // fusion
        Map<Long, UserProfilDTO> profilsMap = profils.stream()
                .collect(Collectors.toMap(
                        UserProfilDTO::getAuthId,
                        p -> p,
                        (a, b) -> a
                ));

        return authUsers.stream()
                .map(user -> {
                    UserProfilDTO profil = profilsMap.get(user.getId());
                    return buildUserComplet(user, profil);
                })
                .toList();
    }

    public UserDTO getUserById(Long authId) {
        User user = userRepository.findById(authId)
                .orElseThrow(() -> new RuntimeException(
                        "Utilisateur introuvable id=" + authId));

        UserProfilDTO profil = null;
        try {
            profil = userServiceClient.getProfilByAuthId(authId);
        } catch (Exception e) {
            log.warn("[AUTH] Profil non récupéré pour authId={} : {}",
                    authId, e.getMessage());
        }

        return buildUserComplet(user, profil);
    }

    private UserDTO buildUserComplet(User user, UserProfilDTO profil) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .authId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt());

        if (profil != null) {
            builder
                    .firstname(profil.getFirstname())
                    .lastname(profil.getLastname())
                    .phone(profil.getPhone());
        }

        return builder.build();
    }
}
