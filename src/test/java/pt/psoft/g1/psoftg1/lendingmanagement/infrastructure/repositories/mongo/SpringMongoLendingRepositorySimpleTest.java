package pt.psoft.g1.psoftg1.lendingmanagement.infrastructure.repositories.mongo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
class SpringMongoLendingRepositorySimpleTest {

    @Autowired
    private SpringMongoLendingRepository repository;

    @Autowired
    private IdGenerationService idGenerationService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    private Lending saveToMongo(Lending lending) {
        try {
            Field pkField = Lending.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long currentId = (Long) pkField.get(lending);
            if (currentId == null || currentId == 0) {
                pkField.set(lending, idGenerationService.generateId());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID for Lending", e);
        }
        return ((MongoRepository<Lending, Long>) repository).save(lending);
    }

    private Book createBook(String isbn, String title) {
        try {
            Author author = new Author("Test Author", "Test bio", null);
            Field authorNumberField = Author.class.getDeclaredField("authorNumber");
            authorNumberField.setAccessible(true);
            authorNumberField.set(author, idGenerationService.generateId());

            Genre genre = new Genre("Fiction");
            Field genrePkField = Genre.class.getDeclaredField("pk");
            genrePkField.setAccessible(true);
            genrePkField.set(genre, idGenerationService.generateId());

            Book book = new Book(isbn, title, "Test description", genre, Arrays.asList(author), null);
            Field bookPkField = Book.class.getDeclaredField("pk");
            bookPkField.setAccessible(true);
            bookPkField.set(book, idGenerationService.generateId());
            
            return book;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create Book", e);
        }
    }

    private ReaderDetails createReaderDetails(int readerNumber, String username) {
        try {
            Reader reader = Reader.newReader(username, "Password1!", "Test Reader");
            Field userIdField = User.class.getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(reader, idGenerationService.generateId());

            Genre genre = new Genre("Fiction");
            Field genrePkField = Genre.class.getDeclaredField("pk");
            genrePkField.setAccessible(true);
            genrePkField.set(genre, idGenerationService.generateId());

            ReaderDetails readerDetails = new ReaderDetails(
                readerNumber,
                reader,
                "1990-01-01",
                "912345678",
                true,
                true,
                false,
                null,
                Arrays.asList(genre)
            );
            
            Field rdPkField = ReaderDetails.class.getDeclaredField("pk");
            rdPkField.setAccessible(true);
            rdPkField.set(readerDetails, idGenerationService.generateId());
            
            return readerDetails;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create ReaderDetails", e);
        }
    }

    @Test
    void testSaveLending() {
        Book book = createBook("9780451524935", "Test Book");
        ReaderDetails readerDetails = createReaderDetails(2024001, "testuser");

        Lending lending = new Lending(book, readerDetails, 1, 15, 50);

        Lending saved = saveToMongo(lending);

        assertNotNull(saved);
        assertEquals("2025/1", saved.getLendingNumber());
        assertNotNull(saved.getStartDate());
        assertNotNull(saved.getLimitDate());
        assertNull(saved.getReturnedDate());
    }

    @Test
    void testFindByLendingNumber() {
        Book book = createBook("9781402894626", "Another Book");
        ReaderDetails readerDetails = createReaderDetails(2024002, "finduser");

        Lending lending = new Lending(book, readerDetails, 2, 15, 50);
        Lending saved = saveToMongo(lending);
        String lendingNumber = saved.getLendingNumber();

        Optional<Lending> found = repository.findByLendingNumber(lendingNumber);

        assertTrue(found.isPresent());
        assertEquals(lendingNumber, found.get().getLendingNumber());
    }

    @Test
    void testSaveMultipleLendings() {
        Book book1 = createBook("9780316769174", "Book One");
        Book book2 = createBook("9780061120084", "Book Two");
        ReaderDetails readerDetails = createReaderDetails(2024003, "multiuser");

        Lending lending1 = new Lending(book1, readerDetails, 3, 15, 50);
        Lending lending2 = new Lending(book2, readerDetails, 4, 15, 50);

        saveToMongo(lending1);
        saveToMongo(lending2);

        assertEquals(2, repository.count());
    }

    @Test
    void testDeleteById() {
        Book book = createBook("9780547928227", "Delete Book");
        ReaderDetails readerDetails = createReaderDetails(2024004, "deleteuser");

        Lending lending = new Lending(book, readerDetails, 5, 15, 50);
        Lending saved = saveToMongo(lending);

        try {
            Field pkField = Lending.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);

            ((MongoRepository<Lending, Long>) repository).deleteById(id);

            Optional<Lending> found = ((MongoRepository<Lending, Long>) repository).findById(id);
            assertFalse(found.isPresent());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testDeleteAll() {
        Book book1 = createBook("9780545010221", "DeleteAll Book 1");
        Book book2 = createBook("9780439023481", "DeleteAll Book 2");
        ReaderDetails readerDetails = createReaderDetails(2024005, "deleteallluser");

        Lending lending1 = new Lending(book1, readerDetails, 6, 15, 50);
        Lending lending2 = new Lending(book2, readerDetails, 7, 15, 50);

        saveToMongo(lending1);
        saveToMongo(lending2);

        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    void testFindByLendingNumberNotFound() {
        Optional<Lending> found = repository.findByLendingNumber("9999/9999");

        assertFalse(found.isPresent());
    }

    @Test
    void testCountLendings() {
        Book book1 = createBook("9780143039433", "Count Book 1");
        Book book2 = createBook("9780061122415", "Count Book 2");
        Book book3 = createBook("9780316015844", "Count Book 3");
        ReaderDetails readerDetails = createReaderDetails(2024006, "countuser");

        Lending lending1 = new Lending(book1, readerDetails, 8, 15, 50);
        Lending lending2 = new Lending(book2, readerDetails, 9, 15, 50);
        Lending lending3 = new Lending(book3, readerDetails, 10, 15, 50);

        saveToMongo(lending1);
        saveToMongo(lending2);
        saveToMongo(lending3);

        assertEquals(3, repository.count());
    }

    @Test
    void testLendingWithDifferentDurations() {
        Book book1 = createBook("9780060850524", "Duration Book 1");
        Book book2 = createBook("9780140449136", "Duration Book 2");
        ReaderDetails readerDetails = createReaderDetails(2024007, "durationuser");

        Lending lending15Days = new Lending(book1, readerDetails, 11, 15, 50);
        Lending lending30Days = new Lending(book2, readerDetails, 12, 30, 50);

        Lending saved15 = saveToMongo(lending15Days);
        Lending saved30 = saveToMongo(lending30Days);

        assertNotNull(saved15.getLimitDate());
        assertNotNull(saved30.getLimitDate());
        assertTrue(saved30.getLimitDate().isAfter(saved15.getLimitDate()));
    }

    @Test
    void testLendingWithDifferentFineValues() {
        Book book1 = createBook("9780062315007", "Fine Book 1");
        Book book2 = createBook("9780553418026", "Fine Book 2");
        ReaderDetails readerDetails = createReaderDetails(2024008, "fineuser");

        Lending lending50Cents = new Lending(book1, readerDetails, 13, 15, 50);
        Lending lending100Cents = new Lending(book2, readerDetails, 14, 15, 100);

        Lending saved50 = saveToMongo(lending50Cents);
        Lending saved100 = saveToMongo(lending100Cents);

        assertEquals(50, saved50.getFineValuePerDayInCents());
        assertEquals(100, saved100.getFineValuePerDayInCents());
    }

    @Test
    void testExistsById() {
        Book book = createBook("9780060935467", "Exists Book");
        ReaderDetails readerDetails = createReaderDetails(2024009, "existsuser");

        Lending lending = new Lending(book, readerDetails, 15, 15, 50);
        Lending saved = saveToMongo(lending);

        try {
            Field pkField = Lending.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);

            boolean exists = ((MongoRepository<Lending, Long>) repository).existsById(id);
            assertTrue(exists);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testFindAll() {
        Book book1 = createBook("9780141439518", "FindAll Book 1");
        Book book2 = createBook("9780547928210", "FindAll Book 2");
        ReaderDetails readerDetails = createReaderDetails(2024010, "findalluser");

        Lending lending1 = new Lending(book1, readerDetails, 16, 15, 50);
        Lending lending2 = new Lending(book2, readerDetails, 17, 15, 50);

        saveToMongo(lending1);
        saveToMongo(lending2);

        List<Lending> all = ((MongoRepository<Lending, Long>) repository).findAll();

        assertEquals(2, all.size());
    }
}
