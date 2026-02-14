package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderBookCountDTO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
public interface SpringMongoReaderRepository extends ReaderRepository, MongoRepository<ReaderDetails, Long> {
    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'readerNumber.readerNumber': ?0 }")
    Optional<ReaderDetails> findByReaderNumber(String readerNumber);

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'phoneNumber.phoneNumber': ?0 }")
    List<ReaderDetails> findByPhoneNumber(String phoneNumber);

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'reader.username': ?0 }")
    Optional<ReaderDetails> findByUsername(String username);

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'reader._id': ?0 }")
    Optional<ReaderDetails> findByUserId(Long userId);

    @Override
    default int getCountFromCurrentYear() { return 0; }

    @Override
    default Page<ReaderDetails> findTopReaders(Pageable pageable) { return Page.empty(); }

    @Override
    default Page<ReaderBookCountDTO> findTopByGenre(Pageable pageable, String genre, LocalDate startDate, LocalDate endDate) { return Page.empty(); }

    @Override
    default List<ReaderDetails> searchReaderDetails(pt.psoft.g1.psoftg1.shared.services.Page page, pt.psoft.g1.psoftg1.readermanagement.services.SearchReadersQuery query) { return Collections.emptyList(); }
}


