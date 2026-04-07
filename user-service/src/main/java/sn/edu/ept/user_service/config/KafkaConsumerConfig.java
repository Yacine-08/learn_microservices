package sn.edu.ept.user_service.config;

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
import sn.edu.ept.user_service.event.UserRegisteredEvent;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> consumerFactory() {
        JsonDeserializer<UserRegisteredEvent> deserializer =
                new JsonDeserializer<>(UserRegisteredEvent.class, false);
        deserializer.addTrustedPackages("sn.edu.ept.user_service.*");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,  bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,            "user-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,   "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,  false);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent>
    kafkaListenerContainerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<
                String, UserRegisteredEvent>();
        factory.setConsumerFactory(consumerFactory());

        // Acquittement manuel → on confirme seulement si le traitement réussit
        factory.getContainerProperties()
                .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Nombre de threads consommateurs
        factory.setConcurrency(2);

        return factory;
    }

}
