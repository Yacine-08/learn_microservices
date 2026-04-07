package sn.edu.ept.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import sn.edu.ept.userservice.repository.UserRepository;
import sn.edu.ept.userservice.user.Role;
import sn.edu.ept.userservice.user.UserProfile;
import sn.edu.ept.userservice.user.UserRegisteredEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final UserRepository userRepository;

    @KafkaListener(
            topics = "user.registered",
            groupId = "user-service"
    )

    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Event reçu — création profil pour : {}", event.email());

        // Vérifier si le profil n'a pas déjà été créé (idempotence)
        if (userRepository.findByAuthId(event.authId()).isPresent()) {
            log.warn("Profil déjà existant pour authId {} ",
                    event.authId());
            return;
        }

        // Créer le profil utilisateur
        var profil = UserProfile.builder()
                .authId(event.authId())
                .firstname(event.firstname())
                .lastname(event.lastname())
                .email(event.email())
                .phone(event.phone())
                .role(Role.valueOf(event.role()))
                .build();

        userRepository.save(profil);
        log.info("Profil créé avec succès pour authId {}", event.authId());
    }
}
