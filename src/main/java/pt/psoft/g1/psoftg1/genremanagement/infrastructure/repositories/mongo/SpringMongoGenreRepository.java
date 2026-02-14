package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.services.GenreBookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsDTO;
import pt.psoft.g1.psoftg1.genremanagement.services.GenreLendingsPerMonthDTO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
public interface SpringMongoGenreRepository extends GenreRepository, MongoRepository<Genre, Long> {

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'genre': ?0 }")
    Optional<Genre> findByString(String genreName);

    @Override
    default Page<GenreBookCountDTO> findTop5GenreByBookCount(Pageable pageable) { return Page.empty(); }

    @Override
    default List<GenreLendingsDTO> getAverageLendingsInMonth(LocalDate month, pt.psoft.g1.psoftg1.shared.services.Page page) { return Collections.emptyList(); }

    @Override
    default List<GenreLendingsPerMonthDTO> getLendingsPerMonthLastYearByGenre() { return Collections.emptyList(); }

    @Override
    default List<GenreLendingsPerMonthDTO> getLendingsAverageDurationPerMonth(LocalDate startDate, LocalDate endDate) { return Collections.emptyList(); }
}


