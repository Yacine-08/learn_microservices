package sn.edu.ept.security.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import sn.edu.ept.security.user.UserUpdatedEvent;
import sn.edu.ept.security.event.UserDeletedEvent;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, UserUpdatedEvent> consumerFactory() {
        JsonDeserializer<UserUpdatedEvent> deserializer =
                new JsonDeserializer<>(UserUpdatedEvent.class, false);
        deserializer.addTrustedPackages("sn.edu.ept.user_service.*");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "auth-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserUpdatedEvent>
    kafkaListenerContainerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, UserUpdatedEvent>();
        factory.setConsumerFactory(consumerFactory());

        // Acquittement manuel
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        factory.setConcurrency(2);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserDeletedEvent> userDeletedEventConsumerFactory() {
        JsonDeserializer<UserDeletedEvent> deserializer =
                new JsonDeserializer<>(UserDeletedEvent.class, false);
        deserializer.addTrustedPackages("sn.edu.ept.user_service.*");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "auth-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserDeletedEvent>
    userDeletedEventKafkaListenerContainerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, UserDeletedEvent>();
        factory.setConsumerFactory(userDeletedEventConsumerFactory());

        // Acquittement manuel
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        factory.setConcurrency(1);

        return factory;
    }
}
