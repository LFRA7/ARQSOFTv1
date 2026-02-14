package pt.psoft.g1.psoftg1.bookmanagement.infrastructure.repositories.mongo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.mongo.SpringMongoAuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.mongo.SpringMongoGenreRepository;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple functional tests for SpringMongoBookRepository
 */
@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
class SpringMongoBookRepositorySimpleTest {

    @Autowired
    private SpringMongoBookRepository bookRepository;

    @Autowired
    private SpringMongoAuthorRepository authorRepository;

    @Autowired
    private SpringMongoGenreRepository genreRepository;

    @Autowired
    private IdGenerationService idGenerationService;

    private Author testAuthor;
    private Genre testGenre;

    // Helper methods to resolve method ambiguity and handle ID generation
    private Book saveToMongo(Book book) {
        try {
            Field pkField = Book.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long currentId = (Long) pkField.get(book);
            if (currentId == null || currentId == 0) {
                pkField.set(book, idGenerationService.generateId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set book ID", e);
        }
        return ((org.springframework.data.mongodb.repository.MongoRepository<Book, Long>) bookRepository).save(book);
    }

    private Author saveAuthor(Author author) {
        try {
            Field authorNumberField = Author.class.getDeclaredField("authorNumber");
            authorNumberField.setAccessible(true);
            Long currentId = (Long) authorNumberField.get(author);
            if (currentId == null || currentId == 0) {
                authorNumberField.set(author, idGenerationService.generateId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set author ID", e);
        }
        return ((org.springframework.data.mongodb.repository.MongoRepository<Author, Long>) authorRepository).save(author);
    }

    private Genre saveGenre(Genre genre) {
        try {
            Field pkField = Genre.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long currentId = (Long) pkField.get(genre);
            if (currentId == null || currentId == 0) {
                pkField.set(genre, idGenerationService.generateId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set genre ID", e);
        }
        return ((org.springframework.data.mongodb.repository.MongoRepository<Genre, Long>) genreRepository).save(genre);
    }

    private void deleteFromMongo(Book book) {
        ((org.springframework.data.mongodb.repository.MongoRepository<Book, Long>) bookRepository).delete(book);
    }

    @BeforeEach
    void setUp() {
        // Clean database
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        // Create test data
        testAuthor = new Author("Test Author", "Test bio", null);
        testAuthor = saveAuthor(testAuthor);

        testGenre = new Genre("Science Fiction");
        testGenre = saveGenre(testGenre);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save book successfully")
    void testSaveBook() {

        Book book = new Book(
                "9780306406157",
                "Test Book",
                "Test description",
                testGenre,
                List.of(testAuthor),
                null
        );

        Book saved = saveToMongo(book);

        assertNotNull(saved);
        try {
            Field pkField = Book.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long pk = (Long) pkField.get(saved);
            assertNotNull(pk);
        } catch (Exception e) {
            fail("Failed to access pk field: " + e.getMessage());
        }
        assertEquals("9780306406157", saved.getIsbn());
        assertEquals("Test Book", saved.getTitle().toString());
    }

    @Test
    @DisplayName("Should save multiple books")
    void testSaveMultipleBooks() {

        Book book1 = new Book("9782826012092", "Book 1", "Desc 1", testGenre, List.of(testAuthor), null);
        Book book2 = new Book("9780306406157", "Book 2", "Desc 2", testGenre, List.of(testAuthor), null);

        saveToMongo(book1);
        saveToMongo(book2);

        assertEquals(2, bookRepository.count());
    }

    @Test
    @DisplayName("Should find book by ISBN")
    void testFindByIsbn() {

        Book book = new Book("9782826012092", "Test Book", "Test description", testGenre, List.of(testAuthor), null);
        saveToMongo(book);

        Optional<Book> found = bookRepository.findByIsbn("9782826012092");

        assertTrue(found.isPresent());
        assertEquals("Test Book", found.get().getTitle().toString());
    }

    @Test
    @DisplayName("Should return empty when ISBN not found")
    void testFindByIsbnNotFound() {
        Optional<Book> found = bookRepository.findByIsbn("9999999999999");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all books")
    void testFindAll() {

        Book book1 = new Book("9782826012092", "Book 1", "Desc 1", testGenre, List.of(testAuthor), null);
        Book book2 = new Book("9780306406157", "Book 2", "Desc 2", testGenre, List.of(testAuthor), null);
        saveToMongo(book1);
        saveToMongo(book2);

        List<Book> all = bookRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Should delete book by ID")
    void testDeleteById() {

        Book book = new Book("9782826012092", "Test Book", "Test description", testGenre, List.of(testAuthor), null);
        Book saved = saveToMongo(book);
        
        try {
            Field pkField = Book.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long bookId = (Long) pkField.get(saved);

            bookRepository.deleteById(bookId);

            assertFalse(bookRepository.existsById(bookId));
        } catch (Exception e) {
            fail("Failed to access pk field: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should delete all books")
    void testDeleteAll() {
        Book book1 = new Book("9782826012092", "Book 1", "Desc 1", testGenre, List.of(testAuthor), null);
        Book book2 = new Book("9780306406157", "Book 2", "Desc 2", testGenre, List.of(testAuthor), null);
        saveToMongo(book1);
        saveToMongo(book2);

        bookRepository.deleteAll();

        assertEquals(0, bookRepository.count());
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should handle book with long description")
    void testLongDescription() {

        String longDesc = "A".repeat(4000);
        Book book = new Book("9782826012092", "Test Book", longDesc, testGenre, List.of(testAuthor), null);

        Book saved = saveToMongo(book);

        assertNotNull(saved);
        assertEquals(longDesc.length(), saved.getDescription().length());
    }

    @Test
    @DisplayName("Should count books correctly")
    void testCount() {

        assertEquals(0, bookRepository.count());

        Book book = new Book("9782826012092", "Test Book", "Test description", testGenre, List.of(testAuthor), null);
        saveToMongo(book);

        assertEquals(1, bookRepository.count());
    }

    @Test
    @DisplayName("Should check existence by ID")
    void testExistsById() {

        Book book = new Book("9782826012092", "Test Book", "Test description", testGenre, List.of(testAuthor), null);
        Book saved = saveToMongo(book);

        try {
            Field pkField = Book.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long bookId = (Long) pkField.get(saved);

            assertTrue(bookRepository.existsById(bookId));

            bookRepository.deleteById(bookId);

            assertFalse(bookRepository.existsById(bookId));
        } catch (Exception e) {
            fail("Failed to access pk field: " + e.getMessage());
        }
    }
}
