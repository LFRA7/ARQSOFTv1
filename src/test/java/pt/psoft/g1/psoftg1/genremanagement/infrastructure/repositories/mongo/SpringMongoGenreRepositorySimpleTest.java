package pt.psoft.g1.psoftg1.genremanagement.infrastructure.repositories.mongo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple functional tests for SpringMongoGenreRepository
 */
@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
class SpringMongoGenreRepositorySimpleTest {

    @Autowired
    private SpringMongoGenreRepository repository;

    @Autowired
    private IdGenerationService idGenerationService;

    // Helper method to resolve method ambiguity and handle ID generation
    private Genre saveToMongo(Genre genre) {
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
        return ((org.springframework.data.mongodb.repository.MongoRepository<Genre, Long>) repository).save(genre);
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save genre successfully")
    void testSaveGenre() {
        
        Genre genre = new Genre("Science Fiction");

        Genre saved = saveToMongo(genre);

        assertNotNull(saved);
        assertEquals("Science Fiction", saved.getGenre());
    }

    @Test
    @DisplayName("Should save multiple genres")
    void testSaveMultipleGenres() {
        
        Genre genre1 = new Genre("Fantasy");
        Genre genre2 = new Genre("Mystery");

        saveToMongo(genre1);
        saveToMongo(genre2);

        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("Should find genre by string")
    void testFindByString() {
        
        Genre genre = new Genre("Horror");
        saveToMongo(genre);

        Optional<Genre> found = repository.findByString("Horror");

        assertTrue(found.isPresent());
        assertEquals("Horror", found.get().getGenre());
    }

    @Test
    @DisplayName("Should return empty when genre not found")
    void testFindByStringNotFound() {
        Optional<Genre> found = repository.findByString("NonExistent");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all genres")
    void testFindAll() {
        
        saveToMongo(new Genre("Romance"));
        saveToMongo(new Genre("Thriller"));

        List<Genre> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Should return empty list when no genres exist")
    void testFindAllEmpty() {

        List<Genre> all = repository.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("Should delete genre by ID")
    void testDeleteById() {
        
        Genre genre = new Genre("Biography");
        Genre saved = saveToMongo(genre);
        
        try {
            Field pkField = Genre.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long genreId = (Long) pkField.get(saved);

            repository.deleteById(genreId);

            assertFalse(repository.existsById(genreId));
        } catch (Exception e) {
            fail("Failed to access pk field: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should delete all genres")
    void testDeleteAll() {
        
        saveToMongo(new Genre("History"));
        saveToMongo(new Genre("Science"));
        assertEquals(2, repository.count());

        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    // ========== EDGE CASES ==========

    @Test
    @DisplayName("Should count genres correctly")
    void testCount() {
        
        assertEquals(0, repository.count());

        saveToMongo(new Genre("Drama"));
        assertEquals(1, repository.count());

        saveToMongo(new Genre("Comedy"));
        assertEquals(2, repository.count());
    }

    @Test
    @DisplayName("Should check existence by ID")
    void testExistsById() {
        
        Genre genre = new Genre("Adventure");
        Genre saved = saveToMongo(genre);

        try {
            Field pkField = Genre.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            Long genreId = (Long) pkField.get(saved);

            assertTrue(repository.existsById(genreId));

            repository.deleteById(genreId);

            assertFalse(repository.existsById(genreId));
        } catch (Exception e) {
            fail("Failed to access pk field: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should handle special characters in genre name")
    void testSpecialCharacters() {
        
        Genre genre = new Genre("Science-Fiction & Fantasy");

        saveToMongo(genre);
        Optional<Genre> found = repository.findByString("Science-Fiction & Fantasy");

        assertTrue(found.isPresent());
        assertEquals("Science-Fiction & Fantasy", found.get().getGenre());
    }

    @Test
    @DisplayName("Should handle long genre names")
    void testLongGenreName() {
        
        String longName = "Historical Science Fiction Adventure Mystery";
        Genre genre = new Genre(longName);

        Genre saved = saveToMongo(genre);

        assertNotNull(saved);
        assertEquals(longName, saved.getGenre());
    }
}
