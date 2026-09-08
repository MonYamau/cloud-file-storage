package ru.monyamau.cloudfilestorage.config;

import io.minio.MinioClient;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@ComponentScan("ru.monyamau.cloudfilestorage")
@PropertySource({"classpath:application.properties"})
@EnableJpaRepositories("ru.monyamau.cloudfilestorage.repository")
@EnableTransactionManagement
public class ApplicationConfig {
    private final static String COOKIE_NAME = "SESSION_ID";

    private final Environment env;

    @Autowired
    public ApplicationConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("mysql.driver_class"));
        dataSource.setUrl(env.getRequiredProperty("mysql.url"));
        dataSource.setUsername(env.getRequiredProperty("mysql.username"));
        dataSource.setPassword(env.getRequiredProperty("mysql.password"));
        return dataSource;
    }

    @Bean
    public RedisConnectionFactory redisFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(
                env.getRequiredProperty("redis.host"),
                Integer.parseInt(env.getRequiredProperty("redis.port")));
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean managerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        managerFactoryBean.setDataSource(dataSource);
        managerFactoryBean.setPackagesToScan("ru.monyamau.cloudfilestorage.entity");
        managerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        return managerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .credentials(env.getRequiredProperty("minio.access_key"), env.getRequiredProperty("minio.secret_key"))
                .endpoint(env.getRequiredProperty("minio.endpoint"))
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                        .title("Cloud file storage API")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("cookie_authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(COOKIE_NAME)))
                .addSecurityItem(new SecurityRequirement()
                        .addList("cookie_authentication"));
    }
}
