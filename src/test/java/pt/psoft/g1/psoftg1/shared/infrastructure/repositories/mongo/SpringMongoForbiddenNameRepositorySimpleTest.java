package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.mongo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.shared.model.ForbiddenName;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
class SpringMongoForbiddenNameRepositorySimpleTest {

    @Autowired
    private SpringMongoForbiddenNameRepository repository;

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

    private ForbiddenName saveToMongo(ForbiddenName forbiddenName) {
        try {
            Field pkField = ForbiddenName.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long currentId = (Long) pkField.get(forbiddenName);
            if (currentId == null || currentId == 0) {
                pkField.set(forbiddenName, idGenerationService.generateId());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID for ForbiddenName", e);
        }
        return ((MongoRepository<ForbiddenName, Long>) repository).save(forbiddenName);
    }

    @Test
    void testSaveForbiddenName() {
        ForbiddenName forbiddenName = new ForbiddenName("badword");

        ForbiddenName saved = saveToMongo(forbiddenName);

        assertNotNull(saved);
        assertEquals("badword", saved.getForbiddenName());
    }

    @Test
    void testFindByForbiddenName() {
        ForbiddenName forbiddenName = new ForbiddenName("offensive");
        saveToMongo(forbiddenName);

        Optional<ForbiddenName> found = repository.findByForbiddenName("offensive");

        assertTrue(found.isPresent());
        assertEquals("offensive", found.get().getForbiddenName());
    }

    @Test
    void testFindByForbiddenNameIsContained() {
        ForbiddenName fn1 = new ForbiddenName("bad");
        ForbiddenName fn2 = new ForbiddenName("good");
        ForbiddenName fn3 = new ForbiddenName("ugly");

        saveToMongo(fn1);
        saveToMongo(fn2);
        saveToMongo(fn3);

        List<ForbiddenName> found = repository.findByForbiddenNameIsContained("This is a bad example");

        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        assertEquals("bad", found.get(0).getForbiddenName());
    }

    @Test
    void testFindByForbiddenNameIsContainedMultiple() {
        ForbiddenName fn1 = new ForbiddenName("bad");
        ForbiddenName fn2 = new ForbiddenName("very");

        saveToMongo(fn1);
        saveToMongo(fn2);

        List<ForbiddenName> found = repository.findByForbiddenNameIsContained("This is very bad");

        assertEquals(2, found.size());
    }

    @Test
    void testDeleteForbiddenName() {
        ForbiddenName forbiddenName = new ForbiddenName("delete");
        saveToMongo(forbiddenName);

        int deleted = repository.deleteForbiddenName("delete");

        assertEquals(1, deleted);
        assertEquals(0, repository.count());
    }

    @Test
    void testDeleteForbiddenNameNotFound() {
        int deleted = repository.deleteForbiddenName("nonexistent");

        assertEquals(0, deleted);
    }

    @Test
    void testSaveMultipleForbiddenNames() {
        ForbiddenName fn1 = new ForbiddenName("word1");
        ForbiddenName fn2 = new ForbiddenName("word2");
        ForbiddenName fn3 = new ForbiddenName("word3");

        saveToMongo(fn1);
        saveToMongo(fn2);
        saveToMongo(fn3);

        assertEquals(3, repository.count());
    }

    @Test
    void testDeleteById() {
        ForbiddenName forbiddenName = new ForbiddenName("deleteme");
        ForbiddenName saved = saveToMongo(forbiddenName);

        try {
            Field pkField = ForbiddenName.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);

            ((MongoRepository<ForbiddenName, Long>) repository).deleteById(id);

            Optional<ForbiddenName> found = ((MongoRepository<ForbiddenName, Long>) repository).findById(id);
            assertFalse(found.isPresent());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testDeleteAll() {
        ForbiddenName fn1 = new ForbiddenName("delete1");
        ForbiddenName fn2 = new ForbiddenName("delete2");

        saveToMongo(fn1);
        saveToMongo(fn2);

        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    void testCountForbiddenNames() {
        ForbiddenName fn1 = new ForbiddenName("count1");
        ForbiddenName fn2 = new ForbiddenName("count2");
        ForbiddenName fn3 = new ForbiddenName("count3");
        ForbiddenName fn4 = new ForbiddenName("count4");
        ForbiddenName fn5 = new ForbiddenName("count5");

        saveToMongo(fn1);
        saveToMongo(fn2);
        saveToMongo(fn3);
        saveToMongo(fn4);
        saveToMongo(fn5);

        assertEquals(5, repository.count());
    }

    @Test
    void testExistsById() {
        ForbiddenName forbiddenName = new ForbiddenName("exists");
        ForbiddenName saved = saveToMongo(forbiddenName);

        try {
            Field pkField = ForbiddenName.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long id = (Long) pkField.get(saved);

            boolean exists = ((MongoRepository<ForbiddenName, Long>) repository).existsById(id);
            assertTrue(exists);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testFindAll() {
        ForbiddenName fn1 = new ForbiddenName("all1");
        ForbiddenName fn2 = new ForbiddenName("all2");

        saveToMongo(fn1);
        saveToMongo(fn2);

        List<ForbiddenName> all = ((MongoRepository<ForbiddenName, Long>) repository).findAll();

        assertEquals(2, all.size());
    }

    @Test
    void testFindByForbiddenNameNotFound() {
        Optional<ForbiddenName> found = repository.findByForbiddenName("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByForbiddenNameIsContainedNoMatch() {
        ForbiddenName fn = new ForbiddenName("specific");
        saveToMongo(fn);

        List<ForbiddenName> found = repository.findByForbiddenNameIsContained("This text has no match");

        assertTrue(found.isEmpty());
    }

    @Test
    void testCaseSensitivity() {
        ForbiddenName fn = new ForbiddenName("BadWord");
        saveToMongo(fn);

        Optional<ForbiddenName> foundExact = repository.findByForbiddenName("BadWord");
        Optional<ForbiddenName> foundLower = repository.findByForbiddenName("badword");

        assertTrue(foundExact.isPresent());
        assertFalse(foundLower.isPresent());
    }
}
