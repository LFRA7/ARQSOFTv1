package pt.psoft.g1.psoftg1.bookmanagement.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Integration Test - Testing BookRepository with real H2 database
 * Tests repository operations with actual database persistence
 */
@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Genre testGenre;
    private Author testAuthor;
    private Book testBook;
    private final String testIsbn = "9782826012092";

    @BeforeEach
    void setUp() {
        testGenre = new Genre("Fiction");
        entityManager.persist(testGenre);

        testAuthor = new Author("Test Author", "Test Bio", null);
        entityManager.persist(testAuthor);

        List<Author> authors = new ArrayList<>();
        authors.add(testAuthor);
        testBook = new Book(testIsbn, "Test Book", "Test Description",
                testGenre, authors, null);
        entityManager.persist(testBook);
        entityManager.flush();
    }

    // Database test: Find book by ISBN
    @Test
    void testFindByIsbn() {
        Optional<Book> found = bookRepository.findByIsbn(testIsbn);

        assertThat(found).isPresent();
        assertThat(found.get().getIsbn()).isEqualTo(testIsbn);
        assertThat(found.get().getTitle().toString()).isEqualTo("Test Book");
    }

    // Database test: Find book by ISBN not found
    @Test
    void testFindByIsbnNotFound() {
        Optional<Book> found = bookRepository.findByIsbn("9999999999999");

        assertThat(found).isEmpty();
    }

    // Database test: Find books by genre
    @Test
    void testFindByGenre() {
        List<Book> books = bookRepository.findByGenre("Fiction");

        assertThat(books).isNotEmpty();
        assertThat(books).hasSize(1);
        assertThat(books.get(0).getGenre().getGenre()).isEqualTo("Fiction");
    }

    // Database test: Find books by title
    @Test
    void testFindByTitle() {
        List<Book> books = bookRepository.findByTitle("Test Book");

        assertThat(books).isNotEmpty();
        assertThat(books.get(0).getTitle().toString()).isEqualTo("Test Book");
    }

    // Database test: Find books by author name
    @Test
    void testFindByAuthorName() {
        List<Book> books = bookRepository.findByAuthorName("Test%");

        assertThat(books).isNotEmpty();
        assertThat(books.get(0).getAuthors()).isNotEmpty();
    }

    // Database test: Save new book
    @Test
    void testSaveNewBook() {
        List<Author> authors = new ArrayList<>();
        authors.add(testAuthor);
        Book newBook = new Book("9780306406157", "New Book", "New Description",
                testGenre, authors, null);

        Book saved = bookRepository.save(newBook);

        assertThat(saved).isNotNull();
        assertThat(saved.getIsbn()).isEqualTo("9780306406157");
        
        Optional<Book> found = bookRepository.findByIsbn("9780306406157");
        assertThat(found).isPresent();
    }

    // Database test: Update existing book
    @Test
    void testUpdateExistingBook() {
        Book book = bookRepository.findByIsbn(testIsbn).orElseThrow();
        Long version = book.getVersion();
        
        // Simulate update
        pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest request = 
            new pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest();
        request.setTitle("Updated Title");
        book.applyPatch(version, request);

        Book updated = bookRepository.save(book);

        assertThat(updated.getTitle().toString()).isEqualTo("Updated Title");
    }

    // Database test: Delete book
    @Test
    void testDeleteBook() {
        Book book = bookRepository.findByIsbn(testIsbn).orElseThrow();
        
        bookRepository.delete(book);
        entityManager.flush();

        Optional<Book> found = bookRepository.findByIsbn(testIsbn);
        assertThat(found).isEmpty();
    }

    // Database test: Find top 5 books lent
    @Test
    void testFindTop5BooksLent() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        Page<BookCountDTO> result = bookRepository.findTop5BooksLent(oneYearAgo, PageRequest.of(0, 5));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotNull();
    }

    // Database test: Find books by author number
    @Test
    void testFindBooksByAuthorNumber() {
        // Get author number from the persisted author
        Long authorNumber = testAuthor.getAuthorNumber();
        
        List<Book> books = bookRepository.findBooksByAuthorNumber(authorNumber);

        assertThat(books).isNotEmpty();
    }

    // Database test: Book with photo persistence
    @Test
    void testBookWithPhotoPersistence() {
        List<Author> authors = new ArrayList<>();
        authors.add(testAuthor);
        Book bookWithPhoto = new Book("9781402894626", "Book with Photo", "Description",
                testGenre, authors, "photo.jpg");
        
        Book saved = bookRepository.save(bookWithPhoto);
        entityManager.flush();
        entityManager.clear();

        Optional<Book> found = bookRepository.findByIsbn("9781402894626");
        assertThat(found).isPresent();
        assertThat(found.get().getPhoto()).isNotNull();
        assertThat(found.get().getPhoto().getPhotoFile()).isEqualTo("photo.jpg");
    }

    // Database test: Book with multiple authors
    @Test
    void testBookWithMultipleAuthors() {
        Author secondAuthor = new Author("Second Author", "Second Bio", null);
        entityManager.persist(secondAuthor);

        List<Author> authors = new ArrayList<>();
        authors.add(testAuthor);
        authors.add(secondAuthor);
        Book multiAuthorBook = new Book("9780451524935", "Multi Author Book", "Description",
                testGenre, authors, null);
        
        Book saved = bookRepository.save(multiAuthorBook);
        entityManager.flush();
        entityManager.clear();

        Optional<Book> found = bookRepository.findByIsbn("9780451524935");
        assertThat(found).isPresent();
        assertThat(found.get().getAuthors()).hasSize(2);
    }

    // Database test: Genre relationship
    @Test
    void testGenreRelationship() {
        Book book = bookRepository.findByIsbn(testIsbn).orElseThrow();

        assertThat(book.getGenre()).isNotNull();
        assertThat(book.getGenre().getGenre()).isEqualTo("Fiction");
        assertThat(book.getGenre()).isEqualTo(testGenre);
    }
}
