package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.repositories.ForbiddenNameRepository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
public interface SpringMongoForbiddenNameRepository extends ForbiddenNameRepository, MongoRepository<ForbiddenName, Long> {

    @Override
    default List<ForbiddenName> findByForbiddenNameIsContained(String pat) {
        // naive implementation: load all and filter
        // Not using @Query to avoid metadata parsing issues
        return findAll().stream().filter(fn -> pat.contains(fn.getForbiddenName())).toList();
    }

    @Override
    @Query("{ 'forbiddenName': ?0 }")
    Optional<ForbiddenName> findByForbiddenName(String forbiddenName);

    @Override
    default int deleteForbiddenName(String forbiddenName) {
        Optional<ForbiddenName> f = findByForbiddenName(forbiddenName);
        if (f.isPresent()) {
            delete(f.get());
            return 1;
        }
        return 0;
    }
}


