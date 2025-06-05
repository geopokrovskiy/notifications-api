package com.geopokrovskiy.it;

import com.geopokrovskiy.config.TestDatabaseConfiguration;
import com.geopokrovskiy.entity.Notification;
import com.geopokrovskiy.entity.NotificationType;
import com.geopokrovskiy.repository.NotificationRepository;
import com.geopokrovskiy.utils.TestUtils;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Data
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestDatabaseConfiguration.class)
public class KafkaIT {

    private static final Network network = Network.newNetwork();

    private String clusterId;

    @Autowired
    private NotificationRepository notificationRepository;

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    )
            .withNetworkAliases("kafka")
            .withNetwork(network);

    static final GenericContainer<?> schemaRegistry = new GenericContainer<>("confluentinc/cp-schema-registry:7.5.1")
            .withNetworkAliases("schema-registry")
            .withExposedPorts(8081)
            .withNetwork(network);

    static final GenericContainer<?> restProxy = new GenericContainer<>("confluentinc/cp-kafka-rest:7.5.1")
            .withExposedPorts(8082)
            .withNetwork(network);

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(new ServerSetup(1025, null, "smtp"))
            .withConfiguration(GreenMailConfiguration
                    .aConfig()
                    .withUser("user", "password"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> kafka.getBootstrapServers());
        registry.add("spring.kafka.properties.schema.registry.url", () -> "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
        registry.add("spring.kafka.consumer.properties.schema.registry.url", () -> "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081));
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> 1025);
        registry.add("spring.mail.username", () -> "user");
        registry.add("spring.mail.password", () -> "password");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
    }

    @BeforeAll
    static void setup() {
        kafka.start();

        schemaRegistry
                .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                .start();

        restProxy
                .withEnv("KAFKA_REST_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                .withEnv("KAFKA_REST_LISTENERS", "http://0.0.0.0:8082")
                .withEnv("KAFKA_REST_SCHEMA_REGISTRY_URL", "http://schema-registry:8081")
                .start();
    }

    @Test
    @Order(1)
    @DisplayName("Test the presence of sign_up topic")
    void testSignUpTopicExists() throws Exception {

        // Get list of clusters
        String url = "http://" + restProxy.getHost() + ":" + restProxy.getMappedPort(8082) + "/v3/clusters";
        ResponseEntity<String> response = new RestTemplate().getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("cluster_id"));
        JSONObject body = new JSONObject(response.getBody());
        JSONObject clusterData = (JSONObject) ((JSONArray) body.get("data")).get(0);
        String clusterId = clusterData.getString("cluster_id");

        // Get topic list
        url = "http://" + restProxy.getHost() + ":" + restProxy.getMappedPort(8082) + "/v3/clusters/" + clusterId + "/topics";
        response = new RestTemplate().getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("data"));
        body = new JSONObject(response.getBody());
        JSONObject topicData = (JSONObject) ((JSONArray) body.get("data")).get(1); // We don't need _schema, so data[1] is requested, not data[0]
        String topicName = topicData.getString("topic_name");


        // sign_up topic should exist since there's a consumer subscribed on this topic
        assertEquals("sign_up", topicName);

    }

    @Test
    @Order(2)
    @DisplayName("Creation of an AVRO schema")
    void testCreateSchemaInSchemaRegistry() {

        // Creation of sign_up AVRO schema
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity requestJson = new HttpEntity(TestUtils.createSchemaRequest, headers);
        String url = "http://" + schemaRegistry.getHost() + ":" + schemaRegistry.getMappedPort(8081) + "/subjects/sign_up-value/versions";
        ResponseEntity<String> response = new RestTemplate().postForEntity(url, requestJson, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("Send notification")
    void testSendNotificationToConsumer() throws Exception {
        int count;
        count = notificationRepository.findAll().size();
        // Check that any notification has not been created yet
        assertEquals(0, count);

        // Creation of a new notification
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/vnd.kafka.avro.v2+json"));
        headers.setAccept(List.of(MediaType.valueOf("application/vnd.kafka.v2+json")));

        HttpEntity requestJson = new HttpEntity(TestUtils.createNotificationRequest, headers);
        String url = "http://" + restProxy.getHost() + ":" + restProxy.getMappedPort(8082) + "/topics/sign_up";
        ResponseEntity<String> response = new RestTemplate().postForEntity(url, requestJson, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Thread.sleep(5000);

        // Check that a new notification has been created
        List<Notification> notifications = notificationRepository.findAll();
        count = notifications.size();
        assertEquals(1, count);


        // Check that the message has been correctly saved
        Notification notification = notifications.get(0);
        try {
            assertEquals("It's a test message. Please ignore it.", notification.getMessage());
            assertEquals("Test", notification.getSubject());
            assertEquals("email@test.com", notification.getNotificationDest());
            assertEquals("SYSTEM", notification.getCreatedBy());
            assertEquals(NotificationType.EMAIL, notification.getNotificationType());
        } finally {
            notificationRepository.delete(notification);
        }

        // Check that an email has been successfully sent
        greenMail.waitForIncomingEmail(1);
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Test", messages[0].getSubject());
        assertEquals("It's a test message. Please ignore it.", messages[0].getContent());

    }

    @AfterAll
    static void cleanup() {
        restProxy.stop();
        schemaRegistry.stop();
        kafka.stop();
    }
}