package com.krainet.notificationservice;

import com.krainet.common.event.EventType;
import com.krainet.common.event.UserEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@SpringBootTest
@SpringJUnitConfig
@EmbeddedKafka(partitions = 1, topics = {"user-events"})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
public class KafkaIntegrationTest {

    private static final String TOPIC = "user-events";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    private Consumer<String, UserEvent> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker)
        );
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                new JsonDeserializer<>(UserEvent.class)
        ).createConsumer();

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void whenUserEventPublished_thenNotificationServiceReceivesIt() {
        // Given
        UserEvent testEvent = UserEvent.builder(
                EventType.USER_CREATED,
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                "USER",
                UUID.randomUUID(),
                "admin"
        ).build();

        // When
        kafkaTemplate.send(TOPIC, testEvent.getUserId().toString(), testEvent);

        // Then
        await().untilAsserted(() -> {
            ConsumerRecord<String, UserEvent> record =
                    KafkaTestUtils.getSingleRecord(consumer, TOPIC);

            assertNotNull(record);
            assertEquals(testEvent.getUserId().toString(), record.key());
            assertEquals(testEvent.getEventId(), record.value().getEventId());
            assertEquals("testuser", record.value().getUsername());
            assertEquals(EventType.USER_CREATED, record.value().getEventType());
        });
    }
}