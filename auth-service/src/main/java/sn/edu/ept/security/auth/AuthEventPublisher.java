package sn.edu.ept.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import sn.edu.ept.security.user.UserRegisteredEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEventPublisher {
    public static final String TOPIC_USER_REGISTERED = "user.registered";

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        try {
            kafkaTemplate.send(TOPIC_USER_REGISTERED, event.email(), event)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.error("Failed to publish user registered event '{}' for {} : {}",
                                    TOPIC_USER_REGISTERED, event.email(), error.getMessage());
                        } else {
                            log.info("User registered event {} published successfully for : {}",
                                    TOPIC_USER_REGISTERED,
                                    event.email());
                        }
                    });
        } catch (Exception e) {
            log.error("Error serializing user registered event", e);
        }
    }
}
