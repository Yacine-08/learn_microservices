package sn.edu.ept.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import sn.edu.ept.security.event.UserDeletedEvent;
import sn.edu.ept.security.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletedEventConsumer {

    private final UserRepository userRepository;

    @KafkaListener(topics = "user.deleted", groupId = "auth-service-group",
            containerFactory = "userDeletedEventKafkaListenerContainerFactory")
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received user deleted event for authId: {}, email: {}", event.authId(), event.email());

        try {
            // Find user by authId
            userRepository.findById(event.authId())
                    .ifPresentOrElse(
                            user -> {
                                // Delete user from auth-service database
                                userRepository.delete(user);
                                log.info("Successfully deleted user from auth-service database for authId: {}, email: {}",
                                        event.authId(), event.email());
                            },
                            () -> {
                                log.warn("User not found in auth-service for authId: {}, email: {}",
                                        event.authId(), event.email());
                            }
                    );
        } catch (Exception e) {
            log.error("Error processing user deleted event for authId: {}", event.authId(), e);
            throw new RuntimeException("Failed to process user deletion", e);
        }
    }
}
