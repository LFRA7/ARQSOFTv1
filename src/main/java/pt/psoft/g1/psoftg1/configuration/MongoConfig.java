package pt.psoft.g1.psoftg1.configuration;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@Profile({"mongodb-redis"})
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
public class MongoConfig {
}


