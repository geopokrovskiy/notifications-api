Notification microservice built with Java 21, Spring Boot, and Kafka.
Handles receiving messages via Kafka, storing them in PostgreSQL, and sending email alerts to users upon registration.
Supports:

Schema-based Kafka (KRaft mode) messages (Avro via Schema Registry and Kafka Rest-proxy)

REST API for retrieving and updating notification statuses

Flyway DB migrations and TestContainers for integration testing
DTOs are shared via a separate common module and imported as a dependency.

The docker-compose pile can be launched in Windows environment using a ./deploy.ps1 script.
