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
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
class SpringMongoFineRepositorySimpleTest {

    @Autowired
    private SpringMongoFineRepository repository;

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

    private Fine saveToMongo(Fine fine) {
        try {
            Field pkField = Fine.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long currentId = (Long) pkField.get(fine);
            if (currentId == null || currentId == 0) {
                pkField.set(fine, idGenerationService.generateId());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID for Fine", e);
        }
        return ((MongoRepository<Fine, Long>) repository).save(fine);
    }

    private Lending createOverdueLending(int seq, int daysDelayed, int fineValuePerDayInCents) {
        try {
            // Create book
            Author author = new Author("Test Author", "Test bio", null);
            Field authorNumberField = Author.class.getDeclaredField("authorNumber");
            authorNumberField.setAccessible(true);
            authorNumberField.set(author, idGenerationService.generateId());

            Genre genre = new Genre("Fiction");
            Field genrePkField = Genre.class.getDeclaredField("pk");
            genrePkField.setAccessible(true);
            genrePkField.set(genre, idGenerationService.generateId());

            Book book = new Book("9780451524935", "Test Book", "Test description", genre, Arrays.asList(author), null);
            Field bookPkField = Book.class.getDeclaredField("pk");
            bookPkField.setAccessible(true);
            bookPkField.set(book, idGenerationService.generateId());

            // Create reader
            Reader reader = Reader.newReader("testuser" + seq, "Password1!", "Test Reader");
            Field userIdField = User.class.getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(reader, idGenerationService.generateId());

            ReaderDetails readerDetails = new ReaderDetails(
                2024000 + seq,
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

            // Create overdue lending
            Lending lending = new Lending(book, readerDetails, seq, 15, fineValuePerDayInCents);
            Field lendingPkField = Lending.class.getDeclaredField("pk");
            lendingPkField.setAccessible(true);
            lendingPkField.set(lending, idGenerationService.generateId());

            // Make lending overdue by adjusting dates
            Field startDateField = Lending.class.getDeclaredField("startDate");
            startDateField.setAccessible(true);
            startDateField.set(lending, LocalDate.now().minusDays(30));

            Field limitDateField = Lending.class.getDeclaredField("limitDate");
            limitDateField.setAccessible(true);
            limitDateField.set(lending, LocalDate.now().minusDays(daysDelayed));

            return lending;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create overdue Lending", e);
        }
    }

    @Test
    void testSaveFine() {
        Lending overdueLending = createOverdueLending(1, 5, 50);
        Fine fine = new Fine(overdueLending);

        Fine saved = saveToMongo(fine);

        assertNotNull(saved);
        assertEquals(250, saved.getCentsValue()); // 5 days * 50 cents
        assertEquals(50, saved.getFineValuePerDayInCents());
    }

    @Test
    void testFindById() {
        Lending overdueLending = createOverdueLending(2, 3, 100);
        Fine fine = new Fine(overdueLending);
        Fine saved = saveToMongo(fine);

        try {
            Field pkField = Fine.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);

            Optional<Fine> found = ((MongoRepository<Fine, Long>) repository).findById(id);

            assertTrue(found.isPresent());
            assertEquals(300, found.get().getCentsValue()); // 3 days * 100 cents
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testSaveMultipleFines() {
        Lending lending1 = createOverdueLending(3, 2, 50);
        Lending lending2 = createOverdueLending(4, 4, 75);

        Fine fine1 = new Fine(lending1);
        Fine fine2 = new Fine(lending2);

        saveToMongo(fine1);
        saveToMongo(fine2);

        assertEquals(2, repository.count());
    }

    @Test
    void testDeleteById() {
        Lending overdueLending = createOverdueLending(5, 1, 50);
        Fine fine = new Fine(overdueLending);
        Fine saved = saveToMongo(fine);

        try {
            Field pkField = Fine.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);

            ((MongoRepository<Fine, Long>) repository).deleteById(id);

            Optional<Fine> found = ((MongoRepository<Fine, Long>) repository).findById(id);
            assertFalse(found.isPresent());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testDeleteAll() {
        Lending lending1 = createOverdueLending(6, 3, 50);
        Lending lending2 = createOverdueLending(7, 5, 50);

        Fine fine1 = new Fine(lending1);
        Fine fine2 = new Fine(lending2);

        saveToMongo(fine1);
        saveToMongo(fine2);

        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Fine> found = ((MongoRepository<Fine, Long>) repository).findById(999999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testFineCalculationDifferentDelays() {
        Lending lending2Days = createOverdueLending(8, 2, 50);
        Lending lending10Days = createOverdueLending(9, 10, 50);

        Fine fine2Days = new Fine(lending2Days);
        Fine fine10Days = new Fine(lending10Days);

        Fine saved2 = saveToMongo(fine2Days);
        Fine saved10 = saveToMongo(fine10Days);

        assertEquals(100, saved2.getCentsValue()); // 2 * 50
        assertEquals(500, saved10.getCentsValue()); // 10 * 50
    }

    @Test
    void testFineCalculationDifferentRates() {
        Lending lending50Cents = createOverdueLending(10, 5, 50);
        Lending lending100Cents = createOverdueLending(11, 5, 100);

        Fine fine50 = new Fine(lending50Cents);
        Fine fine100 = new Fine(lending100Cents);

        Fine saved50 = saveToMongo(fine50);
        Fine saved100 = saveToMongo(fine100);

        assertEquals(250, saved50.getCentsValue()); // 5 * 50
        assertEquals(500, saved100.getCentsValue()); // 5 * 100
    }

    @Test
    void testCountFines() {
        Lending lending1 = createOverdueLending(12, 1, 50);
        Lending lending2 = createOverdueLending(13, 2, 50);
        Lending lending3 = createOverdueLending(14, 3, 50);

        Fine fine1 = new Fine(lending1);
        Fine fine2 = new Fine(lending2);
        Fine fine3 = new Fine(lending3);

        saveToMongo(fine1);
        saveToMongo(fine2);
        saveToMongo(fine3);

        assertEquals(3, repository.count());
    }

    @Test
    void testExistsById() {
        Lending overdueLending = createOverdueLending(15, 4, 50);
        Fine fine = new Fine(overdueLending);
        Fine saved = saveToMongo(fine);

        try {
            Field pkField = Fine.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);

            boolean exists = ((MongoRepository<Fine, Long>) repository).existsById(id);
            assertTrue(exists);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testFindAll() {
        Lending lending1 = createOverdueLending(16, 2, 50);
        Lending lending2 = createOverdueLending(17, 3, 50);

        Fine fine1 = new Fine(lending1);
        Fine fine2 = new Fine(lending2);

        saveToMongo(fine1);
        saveToMongo(fine2);

        List<Fine> all = ((MongoRepository<Fine, Long>) repository).findAll();

        assertEquals(2, all.size());
    }
}
