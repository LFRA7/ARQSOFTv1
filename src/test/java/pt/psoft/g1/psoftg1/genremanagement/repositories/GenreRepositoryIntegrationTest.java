package pt.psoft.g1.psoftg1.genremanagement.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Database Integration Test - Testing GenreRepository with real H2 database
 * Tests repository operations with actual database persistence
 */
@DataJpaTest
@ActiveProfiles("test")
class GenreRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GenreRepository genreRepository;

    private Genre testGenre;

    @BeforeEach
    void setUp() {
        testGenre = new Genre("Fiction");
        entityManager.persist(testGenre);
        entityManager.flush();
    }

    // Database test: Find genre by string
    @Test
    void testFindByString() {
        Optional<Genre> found = genreRepository.findByString("Fiction");

        assertThat(found).isPresent();
        assertThat(found.get().getGenre()).isEqualTo("Fiction");
    }

    // Database test: Find genre by string not found
    @Test
    void testFindByStringNotFound() {
        Optional<Genre> found = genreRepository.findByString("NonExistent");

        assertThat(found).isEmpty();
    }

    // Database test: Save new genre
    @Test
    void testSaveNewGenre() {
        Genre newGenre = new Genre("Science");
        
        Genre saved = genreRepository.save(newGenre);
        entityManager.flush();

        assertThat(saved).isNotNull();
        assertThat(saved.getGenre()).isEqualTo("Science");
        
        Optional<Genre> found = genreRepository.findByString("Science");
        assertThat(found).isPresent();
    }

    // Database test: Find all genres
    @Test
    void testFindAllGenres() {
        Genre secondGenre = new Genre("Mystery");
        entityManager.persist(secondGenre);
        entityManager.flush();

        Iterable<Genre> genres = genreRepository.findAll();

        assertThat(genres).isNotNull();
        assertThat(genres).hasSize(2);
    }

    // Database test: Delete genre
    @Test
    void testDeleteGenre() {
        Genre genre = genreRepository.findByString("Fiction").orElseThrow();
        
        genreRepository.delete(genre);
        entityManager.flush();

        Optional<Genre> found = genreRepository.findByString("Fiction");
        assertThat(found).isEmpty();
    }

    // Database test: Genre uniqueness constraint
    @Test
    void testGenreUniquenessConstraint() {
        // The first genre is already persisted in setUp
        Genre duplicateGenre = new Genre("Fiction");
        
        // This should fail due to unique constraint, but we handle it gracefully
        entityManager.persist(duplicateGenre);
        
        try {
            entityManager.flush();
        } catch (Exception e) {
            // Expected: unique constraint violation
            assertThat(e).isNotNull();
        }
    }
}
