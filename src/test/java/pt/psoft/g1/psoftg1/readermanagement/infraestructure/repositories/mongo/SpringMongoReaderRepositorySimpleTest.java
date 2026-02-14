package pt.psoft.g1.psoftg1.readermanagement.infraestructure.repositories.mongo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
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
class SpringMongoReaderRepositorySimpleTest {

    @Autowired
    private SpringMongoReaderRepository repository;

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

    private ReaderDetails saveToMongo(ReaderDetails readerDetails) {
        try {
            Field pkField = ReaderDetails.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long currentId = (Long) pkField.get(readerDetails);
            if (currentId == null || currentId == 0) {
                pkField.set(readerDetails, idGenerationService.generateId());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID for ReaderDetails", e);
        }
        return ((MongoRepository<ReaderDetails, Long>) repository).save(readerDetails);
    }

    private Reader createReaderUser(String username, String fullName) {
        try {
            Reader reader = Reader.newReader(username, "Password1!", fullName);
            // User class (parent) has the id field
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(reader, idGenerationService.generateId());
            return reader;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create Reader", e);
        }
    }

    private Genre createGenre(String genreName) {
        try {
            Genre genre = new Genre(genreName);
            Field pkField = Genre.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            pkField.set(genre, idGenerationService.generateId());
            return genre;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create Genre", e);
        }
    }

    @Test
    void testSaveReaderDetails() {
        Reader reader = createReaderUser("john123", "John Doe");
        Genre fiction = createGenre("Fiction");
        
        ReaderDetails readerDetails = new ReaderDetails(
            2024001,
            reader,
            "1990-05-15",
            "912345678",
            true,
            true,
            false,
            null,
            Arrays.asList(fiction)
        );

        ReaderDetails saved = saveToMongo(readerDetails);

        assertNotNull(saved);
        assertEquals("2025/2024001", saved.getReaderNumber()); // Current year + number
        assertEquals("912345678", saved.getPhoneNumber());
        assertTrue(saved.isGdprConsent());
        assertTrue(saved.isMarketingConsent());
        assertFalse(saved.isThirdPartySharingConsent());
    }

    @Test
    void testFindByReaderNumber() {
        Reader reader = createReaderUser("jane456", "Jane Smith");
        Genre scifi = createGenre("SciFi");
        
        ReaderDetails readerDetails = new ReaderDetails(
            2024002,
            reader,
            "1985-03-20",
            "923456789",
            true,
            false,
            false,
            null,
            Arrays.asList(scifi)
        );

        ReaderDetails saved = saveToMongo(readerDetails);
        String readerNumber = saved.getReaderNumber();

        Optional<ReaderDetails> found = repository.findByReaderNumber(readerNumber);

        assertTrue(found.isPresent());
        assertEquals(readerNumber, found.get().getReaderNumber());
        assertEquals("923456789", found.get().getPhoneNumber());
    }

    @Test
    void testFindByPhoneNumber() {
        Reader reader = createReaderUser("bob789", "Bob Johnson");
        Genre fantasy = createGenre("Fantasy");
        
        ReaderDetails readerDetails = new ReaderDetails(
            2024003,
            reader,
            "1995-07-10",
            "934567890",
            true,
            true,
            true,
            null,
            Arrays.asList(fantasy)
        );

        saveToMongo(readerDetails);

        List<ReaderDetails> found = repository.findByPhoneNumber("934567890");

        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        assertEquals("934567890", found.get(0).getPhoneNumber());
    }

    @Test
    void testFindAll() {
        Reader reader = createReaderUser("alice321", "Alice Wonder");
        Genre mystery = createGenre("Mystery");
        
        ReaderDetails readerDetails = new ReaderDetails(
            2024004,
            reader,
            "1992-11-25",
            "945678901",
            true,
            false,
            true,
            null,
            Arrays.asList(mystery)
        );

        saveToMongo(readerDetails);

        List<ReaderDetails> all = ((MongoRepository<ReaderDetails, Long>) repository).findAll();

        assertFalse(all.isEmpty());
        assertEquals(1, all.size());
        assertEquals("945678901", all.get(0).getPhoneNumber());
    }

    @Test
    void testExistsById() {
        Reader reader = createReaderUser("charlie654", "Charlie Brown");
        Genre horror = createGenre("Horror");
        
        ReaderDetails readerDetails = new ReaderDetails(
            2024005,
            reader,
            "1988-02-14",
            "956789012",
            true,
            true,
            false,
            null,
            Arrays.asList(horror)
        );

        ReaderDetails saved = saveToMongo(readerDetails);
        
        try {
            Field pkField = ReaderDetails.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);
            
            boolean exists = ((MongoRepository<ReaderDetails, Long>) repository).existsById(id);
            assertTrue(exists);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testSaveMultipleReaders() {
        Reader reader1 = createReaderUser("user1", "User One");
        Reader reader2 = createReaderUser("user2", "User Two");
        Genre genre1 = createGenre("Romance");
        Genre genre2 = createGenre("Thriller");

        ReaderDetails rd1 = new ReaderDetails(2024006, reader1, "1990-01-01", "911111111", true, true, true, null, Arrays.asList(genre1));
        ReaderDetails rd2 = new ReaderDetails(2024007, reader2, "1991-02-02", "922222222", true, false, false, null, Arrays.asList(genre2));

        saveToMongo(rd1);
        saveToMongo(rd2);

        assertEquals(2, repository.count());
    }

    @Test
    void testDeleteById() {
        Reader reader = createReaderUser("temp123", "Temp User");
        Genre genre = createGenre("Biography");
        
        ReaderDetails readerDetails = new ReaderDetails(
            2024008,
            reader,
            "1993-06-18",
            "967890123",
            true,
            false,
            false,
            null,
            Arrays.asList(genre)
        );

        ReaderDetails saved = saveToMongo(readerDetails);
        
        try {
            Field pkField = ReaderDetails.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);
            
            ((MongoRepository<ReaderDetails, Long>) repository).deleteById(id);

            Optional<ReaderDetails> found = ((MongoRepository<ReaderDetails, Long>) repository).findById(id);
            assertFalse(found.isPresent());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testDeleteAll() {
        Reader reader1 = createReaderUser("delete1", "Delete One");
        Reader reader2 = createReaderUser("delete2", "Delete Two");
        Genre genre = createGenre("Travel");

        ReaderDetails rd1 = new ReaderDetails(2024009, reader1, "1994-08-22", "978901234", true, true, false, null, Arrays.asList(genre));
        ReaderDetails rd2 = new ReaderDetails(2024010, reader2, "1995-09-30", "989012345", true, false, true, null, Arrays.asList(genre));

        saveToMongo(rd1);
        saveToMongo(rd2);

        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    void testReaderWithMultipleGenres() {
        Reader reader = createReaderUser("multigenre", "Multi Genre");
        Genre fiction = createGenre("Fiction");
        Genre mystery = createGenre("Mystery");
        Genre thriller = createGenre("Thriller");

        ReaderDetails readerDetails = new ReaderDetails(
            2024011,
            reader,
            "1987-12-05",
            "990123456",
            true,
            true,
            true,
            null,
            Arrays.asList(fiction, mystery, thriller)
        );

        ReaderDetails saved = saveToMongo(readerDetails);

        assertNotNull(saved.getInterestList());
        assertEquals(3, saved.getInterestList().size());
    }

    @Test
    void testReaderWithNoPhoto() {
        Reader reader = createReaderUser("nophoto", "No Photo User");
        Genre genre = createGenre("Poetry");

        ReaderDetails readerDetails = new ReaderDetails(
            2024012,
            reader,
            "1996-04-11",
            "901234567",
            true,
            false,
            false,
            null,
            Arrays.asList(genre)
        );

        ReaderDetails saved = saveToMongo(readerDetails);

        assertNotNull(saved);
        assertNull(saved.getPhoto());
    }

    @Test
    void testFindByReaderNumberNotFound() {
        Optional<ReaderDetails> found = repository.findByReaderNumber("9999/9999");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByPhoneNumberNotFound() {
        List<ReaderDetails> found = repository.findByPhoneNumber("999999999");

        assertTrue(found.isEmpty());
    }

    @Test
    void testCountReaders() {
        Reader reader1 = createReaderUser("count1", "Count One");
        Reader reader2 = createReaderUser("count2", "Count Two");
        Reader reader3 = createReaderUser("count3", "Count Three");
        Genre genre = createGenre("Science");

        ReaderDetails rd1 = new ReaderDetails(2024013, reader1, "1990-01-01", "912340001", true, true, true, null, Arrays.asList(genre));
        ReaderDetails rd2 = new ReaderDetails(2024014, reader2, "1991-02-02", "912340002", true, false, false, null, Arrays.asList(genre));
        ReaderDetails rd3 = new ReaderDetails(2024015, reader3, "1992-03-03", "912340003", true, true, false, null, Arrays.asList(genre));

        saveToMongo(rd1);
        saveToMongo(rd2);
        saveToMongo(rd3);

        assertEquals(3, repository.count());
    }
}
