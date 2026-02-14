package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;

import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
public interface SpringMongoFineRepository extends FineRepository, MongoRepository<Fine, Long> {
    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'lending.lendingNumber.lendingNumber': ?0 }")
    Optional<Fine> findByLendingNumber(String lendingNumber);
}


