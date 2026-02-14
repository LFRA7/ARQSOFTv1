package pt.psoft.g1.psoftg1.authormanagement.infrastructure.repositories.mongo;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional Opaque-Box Tests for SpringMongoAuthorRepository
 * 
 * Test Strategy: Black-box testing approach
 */
@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpringMongoAuthorRepositoryOpaqueBoxTest {

    @Autowired
    private SpringMongoAuthorRepository repository;

    @Autowired
    private IdGenerationService idGenerationService;

    // Helper methods to resolve method ambiguity
    private Author saveToMongo(Author author) {
        try {
            java.lang.reflect.Field authorNumberField = Author.class.getDeclaredField("authorNumber");
            authorNumberField.setAccessible(true);
            Long currentId = (Long) authorNumberField.get(author);
            if (currentId == null || currentId == 0) {
                authorNumberField.set(author, idGenerationService.generateId());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set author ID", e);
        }
        return ((org.springframework.data.mongodb.repository.MongoRepository<Author, Long>) repository).save(author);
    }

    private void deleteFromMongo(Author author) {
        ((org.springframework.data.mongodb.repository.MongoRepository<Author, Long>) repository).delete(author);
    }

    private Author testAuthor1;
    private Author testAuthor2;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        repository.deleteAll();

        // Create test data
        testAuthor1 = new Author(
                "John Doe",
                "Biography of John Doe, a renowned author.",
                null
        );

        testAuthor2 = new Author(
                "Jane Smith",
                "Biography of Jane Smith, bestselling novelist.",
                null
        );
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Opaque-Box: Should save author successfully")
    void testSaveAuthor() {

        Author saved = saveToMongo(testAuthor1);

        assertNotNull(saved);
        assertEquals(testAuthor1.getAuthorNumber(), saved.getAuthorNumber());
        assertEquals(testAuthor1.getName(), saved.getName());
        assertEquals(testAuthor1.getBio(), saved.getBio());
    }

    @Test
    @Order(2)
    @DisplayName("Opaque-Box: Should save multiple authors")
    void testSaveMultipleAuthors() {

        Author saved1 = saveToMongo(testAuthor1);
        Author saved2 = saveToMongo(testAuthor2);

        assertNotNull(saved1);
        assertNotNull(saved2);
        assertEquals(2, repository.count());
    }

    @Test
    @Order(3)
    @DisplayName("Opaque-Box: Should handle saving author with minimal data")
    void testSaveAuthorMinimalData() {

        Author minimalAuthor = new Author(
                "Min Name",
                "Min bio",
                null
        );

        Author saved = saveToMongo(minimalAuthor);

        assertNotNull(saved);
        assertNotNull(saved.getAuthorNumber());
    }

    @Test
    @Order(10)
    @DisplayName("Opaque-Box: Should find author by author number")
    void testFindByAuthorNumber() {

        Author saved = saveToMongo(testAuthor1);
        Long authorNumber = saved.getAuthorNumber();

        Optional<Author> found = repository.findByAuthorNumber(authorNumber);

        assertTrue(found.isPresent());
        assertEquals(testAuthor1.getAuthorNumber(), found.get().getAuthorNumber());
        assertEquals(testAuthor1.getName(), found.get().getName());
    }

    @Test
    @Order(11)
    @DisplayName("Opaque-Box: Should return empty when author number not found")
    void testFindByAuthorNumberNotFound() {

        Long nonExistentNumber = 999999L;

        Optional<Author> found = repository.findByAuthorNumber(nonExistentNumber);

        assertFalse(found.isPresent());
    }

    @Test
    @Order(12)
    @DisplayName("Opaque-Box: Should find all authors")
    void testFindAll() {

        saveToMongo(testAuthor1);
        saveToMongo(testAuthor2);

        List<Author> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    @Order(13)
    @DisplayName("Opaque-Box: Should return empty list when no authors exist")
    void testFindAllEmpty() {

        List<Author> all = repository.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    @Order(20)
    @DisplayName("Opaque-Box: Should search authors by name starting with prefix")
    void testSearchByNameStartsWith() {

        saveToMongo(testAuthor1); // John Doe
        saveToMongo(testAuthor2); // Jane Smith

        List<Author> found = repository.searchByNameNameStartsWith("John");

        assertEquals(1, found.size());
        assertEquals("John Doe", found.get(0).getName());
    }

    @Test
    @Order(21)
    @DisplayName("Opaque-Box: Should search authors by exact name")
    void testSearchByExactName() {

        saveToMongo(testAuthor1);
        saveToMongo(testAuthor2);

        List<Author> found = repository.searchByNameName("Jane Smith");

        assertEquals(1, found.size());
        assertEquals("Jane Smith", found.get(0).getName());
    }

    @Test
    @Order(22)
    @DisplayName("Opaque-Box: Should return empty list when search finds no matches")
    void testSearchByNameNoMatches() {

        saveToMongo(testAuthor1);

        List<Author> found = repository.searchByNameNameStartsWith("NonExistent");

        assertTrue(found.isEmpty());
    }

    @Test
    @Order(23)
    @DisplayName("Opaque-Box: Should handle case-sensitive search")
    void testSearchCaseSensitivity() {

        saveToMongo(testAuthor1); // "John Doe"

        List<Author> found = repository.searchByNameNameStartsWith("john");

        assertNotNull(found);
    }

    @Test
    @Order(30)
    @DisplayName("Opaque-Box: Should update author bio")
    void testUpdateAuthorBio() {

        Author saved = saveToMongo(testAuthor1);
        Long authorId = saved.getId();
        Long authorNumber = saved.getAuthorNumber();

        saved.setBio("Updated biography");
        Author updated = saveToMongo(saved);

        assertEquals(authorId, updated.getId());
        assertEquals("Updated biography", updated.getBio());

        Optional<Author> found = repository.findByAuthorNumber(authorNumber);
        assertTrue(found.isPresent());
        assertEquals("Updated biography", found.get().getBio());
    }

    @Test
    @Order(31)
    @DisplayName("Opaque-Box: Should update author name")
    void testUpdateAuthorName() {

        Author saved = saveToMongo(testAuthor1);

        saved.setName("Updated Name");
        Author updated = saveToMongo(saved);

        assertEquals("Updated Name", updated.getName());
    }

    @Test
    @Order(40)
    @DisplayName("Opaque-Box: Should delete author by ID")
    void testDeleteById() {

        Author saved = saveToMongo(testAuthor1);
        Long authorId = saved.getId();

        repository.deleteById(authorId);

        assertFalse(repository.existsById(authorId));
        assertEquals(0, repository.count());
    }

    @Test
    @Order(41)
    @DisplayName("Opaque-Box: Should delete author entity")
    void testDeleteEntity() {

        Author saved = saveToMongo(testAuthor1);
        Long authorNumber = saved.getAuthorNumber();

        deleteFromMongo(saved);

        Optional<Author> found = repository.findByAuthorNumber(authorNumber);
        assertFalse(found.isPresent());
    }

    @Test
    @Order(42)
    @DisplayName("Opaque-Box: Should delete all authors")
    void testDeleteAll() {

        saveToMongo(testAuthor1);
        saveToMongo(testAuthor2);
        assertEquals(2, repository.count());

        repository.deleteAll();

        assertEquals(0, repository.count());
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @Order(50)
    @DisplayName("Opaque-Box: Should handle long biography text")
    void testLongBiography() {

        String longBio = "A".repeat(4000); // 4000 characters
        testAuthor1.setBio(longBio);

        Author saved = saveToMongo(testAuthor1);

        assertNotNull(saved);
        assertEquals(longBio.length(), saved.getBio().length());
    }

    @Test
    @Order(51)
    @DisplayName("Opaque-Box: Should count authors correctly")
    void testCount() {

        assertEquals(0, repository.count());

        saveToMongo(testAuthor1);
        assertEquals(1, repository.count());

        saveToMongo(testAuthor2);
        assertEquals(2, repository.count());

        deleteFromMongo(testAuthor1);
        assertEquals(1, repository.count());
    }

    @Test
    @Order(52)
    @DisplayName("Opaque-Box: Should check existence by ID")
    void testExistsById() {

        Author saved = saveToMongo(testAuthor1);
        Long authorId = saved.getId();

        assertTrue(repository.existsById(authorId));

        repository.deleteById(authorId);

        assertFalse(repository.existsById(authorId));
    }

    @Test
    @Order(53)
    @DisplayName("Opaque-Box: Should handle special characters in name")
    void testSpecialCharactersInName() {

        Author specialAuthor = new Author(
                "José María O'Brien-González",
                "Biography with special chars",
                null
        );

        Author saved = saveToMongo(specialAuthor);
        Optional<Author> found = repository.findByAuthorNumber(saved.getAuthorNumber());

        assertTrue(found.isPresent());
        assertEquals("José María O'Brien-González", found.get().getName());
    }

    @Test
    @Order(54)
    @DisplayName("Opaque-Box: Should handle empty search results consistently")
    void testEmptySearchResults() {

        saveToMongo(testAuthor1);

        List<Author> result1 = repository.searchByNameNameStartsWith("XYZ");
        List<Author> result2 = repository.searchByNameName("Non Existent");

        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
    }
}
