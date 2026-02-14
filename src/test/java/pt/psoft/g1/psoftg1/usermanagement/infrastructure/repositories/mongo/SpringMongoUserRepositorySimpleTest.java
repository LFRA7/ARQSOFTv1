package pt.psoft.g1.psoftg1.usermanagement.infrastructure.repositories.mongo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.model.User;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
class SpringMongoUserRepositorySimpleTest {

    @Autowired
    private SpringMongoUserRepository repository;

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

    private User saveToMongo(User user) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            Long currentId = (Long) idField.get(user);
            if (currentId == null || currentId == 0) {
                idField.set(user, idGenerationService.generateId());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID for User", e);
        }
        return repository.save(user);
    }

    @Test
    void testSaveReader() {
        Reader reader = Reader.newReader("johndoe", "Password1!", "John Doe");

        User saved = saveToMongo(reader);

        assertNotNull(saved);
        assertEquals("johndoe", saved.getUsername());
        assertEquals("John Doe", saved.getName().toString());
        assertTrue(saved.isEnabled());
    }

    @Test
    void testSaveLibrarian() {
        Librarian librarian = Librarian.newLibrarian("admin123", "AdminPass1!", "Admin User");

        User saved = saveToMongo(librarian);

        assertNotNull(saved);
        assertEquals("admin123", saved.getUsername());
        assertEquals("Admin User", saved.getName().toString());
        assertTrue(saved.isEnabled());
    }

    @Test
    void testFindById() {
        Reader reader = Reader.newReader("findme", "Password1!", "Find Me");
        User saved = saveToMongo(reader);
        Long id = saved.getId();

        Optional<User> found = repository.findById(id);

        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals("findme", found.get().getUsername());
    }

    @Test
    void testFindByUsername() {
        Reader reader = Reader.newReader("uniqueuser", "Password1!", "Unique User");
        saveToMongo(reader);

        Optional<User> found = repository.findByUsername("uniqueuser");

        assertTrue(found.isPresent());
        assertEquals("uniqueuser", found.get().getUsername());
        assertEquals("Unique User", found.get().getName().toString());
    }

    @Test
    void testFindByNameName() {
        Reader reader1 = Reader.newReader("user1", "Password1!", "Alice Smith");
        Reader reader2 = Reader.newReader("user2", "Password1!", "Bob Smith");
        saveToMongo(reader1);
        saveToMongo(reader2);

        List<User> found = repository.findByNameName("Alice Smith");

        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        assertEquals("Alice Smith", found.get(0).getName().toString());
    }

    @Test
    void testSaveMultipleUsers() {
        Reader reader1 = Reader.newReader("multi1", "Password1!", "Multi One");
        Reader reader2 = Reader.newReader("multi2", "Password1!", "Multi Two");
        Librarian librarian = Librarian.newLibrarian("libmulti", "Password1!", "Lib Multi");

        saveToMongo(reader1);
        saveToMongo(reader2);
        saveToMongo(librarian);

        assertEquals(3, repository.count());
    }

    @Test
    void testDeleteById() {
        Reader reader = Reader.newReader("deleteme", "Password1!", "Delete Me");
        User saved = saveToMongo(reader);
        Long id = saved.getId();

        repository.deleteById(id);

        Optional<User> found = repository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteAll() {
        Reader reader1 = Reader.newReader("delete1", "Password1!", "Delete One");
        Reader reader2 = Reader.newReader("delete2", "Password1!", "Delete Two");

        saveToMongo(reader1);
        saveToMongo(reader2);

        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<User> found = repository.findByUsername("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<User> found = repository.findById(999999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testCountUsers() {
        Reader reader1 = Reader.newReader("count1", "Password1!", "Count One");
        Reader reader2 = Reader.newReader("count2", "Password1!", "Count Two");
        Reader reader3 = Reader.newReader("count3", "Password1!", "Count Three");

        saveToMongo(reader1);
        saveToMongo(reader2);
        saveToMongo(reader3);

        assertEquals(3, repository.count());
    }

    @Test
    void testExistsById() {
        Reader reader = Reader.newReader("exists", "Password1!", "Exists User");
        User saved = saveToMongo(reader);
        Long id = saved.getId();

        assertTrue(repository.existsById(id));
    }

    @Test
    void testFindAll() {
        Reader reader1 = Reader.newReader("all1", "Password1!", "All One");
        Reader reader2 = Reader.newReader("all2", "Password1!", "All Two");

        saveToMongo(reader1);
        saveToMongo(reader2);

        List<User> all = (List<User>) repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void testUserEnabled() {
        Reader reader = Reader.newReader("enabled", "Password1!", "Enabled User");
        User saved = saveToMongo(reader);

        assertTrue(saved.isEnabled());
    }
}
