package sn.edu.ept.user_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletedEventPublisher {

    private final KafkaTemplate<String, UserDeletedEvent> kafkaTemplate;
    private static final String TOPIC = "user.deleted";

    public void publishUserDeleted(UserDeletedEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.authId().toString(), event);
            log.info("Published user deleted event for authId: {}", event.authId());
        } catch (Exception e) {
            log.error("Error publishing user deleted event for authId: {}", event.authId(), e);
            throw new RuntimeException("Failed to publish user deleted event", e);
        }
    }
}
