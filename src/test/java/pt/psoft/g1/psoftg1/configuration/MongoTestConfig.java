package pt.psoft.g1.psoftg1.configuration;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration for MongoDB repositories using Testcontainers
 * Used with @Profile("mongotest") in tests
 * 
 * Testcontainers will automatically start a MongoDB Docker container
 * and configure Spring Boot to connect to it via @ServiceConnection
 * 
 * This configuration re-imports MongoDB auto-configurations that are excluded
 * in the main PsoftG1Application class
 */
@TestConfiguration
@Profile("mongotest")
@ImportAutoConfiguration({
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    MongoRepositoriesAutoConfiguration.class
})
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = {
        "pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.mongo",
        "pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.mongo",
        "pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.mongo",
        "pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.mongo",
        "pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.mongo",
        "pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.mongo",
        "pt.psoft.g1.psoftg1.shared.infrastructure.repositories.mongo"
})
public class MongoTestConfig {
    
    /**
     * Creates a MongoDB Testcontainer with fixed port binding
     * @ServiceConnection automatically configures Spring Boot's MongoDB connection
     */
    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
                .withExposedPorts(27017);
        
        // Start container and configure to use a different port to avoid conflict
        // with existing MongoDB on port 27017
        container.start();
        
        return container;
    }
}
