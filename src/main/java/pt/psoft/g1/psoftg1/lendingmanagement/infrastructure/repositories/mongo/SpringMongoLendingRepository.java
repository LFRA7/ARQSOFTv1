package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
public interface SpringMongoLendingRepository extends LendingRepository, MongoRepository<Lending, Long> {
    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'lendingNumber.lendingNumber': ?0 }")
    Optional<Lending> findByLendingNumber(String lendingNumber);

    @Override
    default List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn) { return Collections.emptyList(); }

    @Override
    default int getCountFromCurrentYear() { return 0; }

    @Override
    default List<Lending> listOutstandingByReaderNumber(String readerNumber) { return Collections.emptyList(); }

    @Override
    default Double getAverageDuration() { return 0.0; }

    @Override
    default Double getAvgLendingDurationByIsbn(String isbn) { return 0.0; }

    @Override
    default List<Lending> getOverdue(Page page) { return Collections.emptyList(); }

    @Override
    default List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate) { return Collections.emptyList(); }
}


