package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.mongo;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Profile({"mongodb-redis", "mongotest"})
public interface SpringMongoBookRepository extends BookRepository, MongoRepository<Book, Long> {

    @Override
    default List<Book> findByGenre(String genre) { return Collections.emptyList(); }

    @Override
    default List<Book> findByTitle(String title) { return Collections.emptyList(); }

    @Override
    default List<Book> findByAuthorName(String authorName) { return Collections.emptyList(); }

    @Override
    @org.springframework.data.mongodb.repository.Query("{ 'isbn.isbn': ?0 }")
    Optional<Book> findByIsbn(String isbn);

    @Override
    default Page<BookCountDTO> findTop5BooksLent(LocalDate oneYearAgo, Pageable pageable) {
        return Page.empty();
    }

    @Override
    default List<Book> findBooksByAuthorNumber(Long authorNumber) { return Collections.emptyList(); }

    @Override
    default List<Book> searchBooks(pt.psoft.g1.psoftg1.shared.services.Page page, pt.psoft.g1.psoftg1.bookmanagement.services.SearchBooksQuery query) {
        return Collections.emptyList();
    }
}


