package sn.edu.ept.security.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.edu.ept.security.user.Role;
import sn.edu.ept.security.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserUpdateListener {

    private final UserRepository userRepository;

    @KafkaListener(topics = "user-updated", groupId = "auth-service-group")
    @Transactional
    public void handleUserUpdate(
            @Payload UserUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received user update event for authId: {}", event.getAuthId());

            userRepository.findById(event.getAuthId())
                    .ifPresentOrElse(user -> {
                        // Mettre à jour les informations de l'utilisateur
                        if (event.getFirstname() != null) {
                            user.setFirstname(event.getFirstname());
                        }
                        if (event.getLastname() != null) {
                            user.setLastname(event.getLastname());
                        }
                        if (event.getPhone() != null) {
                            user.setPhone(event.getPhone());
                        }
                        if (event.getEmail() != null) {
                            user.setEmail(event.getEmail());
                        }
                        if (event.getRole() != null) {
                            user.setRole(Role.valueOf(event.getRole()));
                        }

                        userRepository.save(user);
                        log.info("Successfully updated user in auth_db for authId: {}", event.getAuthId());
                    }, () -> {
                        log.warn("User with authId {} not found in auth_db", event.getAuthId());
                    });

            // Acquitter le message seulement après traitement réussi
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing user update event for authId: {}", event.getAuthId(), e);
            // Ne pas acquitter en cas d'erreur pour permettre une nouvelle tentative
            throw e;
        }
    }
}
