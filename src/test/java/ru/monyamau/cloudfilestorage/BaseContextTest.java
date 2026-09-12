package ru.monyamau.cloudfilestorage;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import ru.monyamau.cloudfilestorage.config.ApplicationConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationConfig.class)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseContextTest {
    @Container
    protected static final MySQLContainer MY_SQL_CONTAINER = new MySQLContainer("mysql:9.7")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @Container
    protected static final GenericContainer REDIS_CONTAINER = new GenericContainer("redis:8.8")
            .withEnv("host", "localhost")
            .withExposedPorts(6379);

    @Container
    protected static final GenericContainer MINIO_CONTAINER = new GenericContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data")
            .withExposedPorts(9000);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("mysql.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("mysql.username", MY_SQL_CONTAINER::getUsername);
        registry.add("mysql.password", MY_SQL_CONTAINER::getPassword);

        registry.add("minio.endpoint", () -> "http://" + MINIO_CONTAINER.getHost() + ":" + MINIO_CONTAINER.getMappedPort(9000));
        registry.add("minio.access_key", () -> "minioadmin");
        registry.add("minio.secret_key", () -> "minioadmin");

        registry.add("redis.host", REDIS_CONTAINER::getHost);
        registry.add("redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }
}
