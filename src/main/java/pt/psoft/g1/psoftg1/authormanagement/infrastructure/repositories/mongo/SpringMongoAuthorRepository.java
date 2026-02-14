package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
public interface SpringMongoAuthorRepository extends AuthorRepository, MongoRepository<Author, Long> {

    @Override
    Optional<Author> findByAuthorNumber(Long authorNumber);

    @Override
    List<Author> searchByNameNameStartsWith(String name);

    @Override
    List<Author> searchByNameName(String name);

    @Override
    default Page<AuthorLendingView> findTopAuthorByLendings(Pageable pageableRules) {
        return Page.empty();
    }

    @Override
    default List<Author> findCoAuthorsByAuthorNumber(Long authorNumber) {
        return Collections.emptyList();
    }
}


