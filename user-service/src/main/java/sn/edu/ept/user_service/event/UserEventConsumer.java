package sn.edu.ept.user_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import sn.edu.ept.user_service.exception.ProfilAlreadyExistException;
import sn.edu.ept.user_service.profile.UserProfileRepository;
import sn.edu.ept.user_service.profile.Role;
import sn.edu.ept.user_service.profile.UserProfile;
import sn.edu.ept.user_service.profile.UserProfileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final UserProfileService service;

    // Ecoute le topic user.registered publié par auth-service
    // groupId pour faire du load balancing lorsque plusieurs instances de user-service tournent
    @KafkaListener(
            topics = "user.registered",
            groupId = "user-service-group"
    )
    public void handleUserRegistered(
            @Payload UserRegisteredEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        try {
            service.createUser(event);

            // Acknowledge le message -> confirmer que le message a été traité
            acknowledgment.acknowledge();
        } catch (ProfilAlreadyExistException e) {
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement utilisateur", e);
        }
    }
}
