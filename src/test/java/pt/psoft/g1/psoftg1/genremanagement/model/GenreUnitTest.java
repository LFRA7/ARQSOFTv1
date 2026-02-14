package pt.psoft.g1.psoftg1.genremanagement.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test - Functional opaque-box testing with SUT = Genre class
 * Tests the Genre domain class in isolation
 */
class GenreUnitTest {

    // Functional opaque-box test: Valid genre creation
    @Test
    void testCreateValidGenre() {
        String genreName = "Fiction";
        Genre genre = new Genre(genreName);
        
        assertNotNull(genre);
        assertEquals(genreName, genre.getGenre());
        assertEquals(genreName, genre.toString());
    }

    // Functional opaque-box test: Genre with maximum length
    @Test
    void testCreateGenreWithMaximumLength() {
        String genreName = "A".repeat(100);
        Genre genre = new Genre(genreName);
        
        assertNotNull(genre);
        assertEquals(100, genre.getGenre().length());
    }

    // Functional opaque-box test: Genre with single character
    @Test
    void testCreateGenreWithSingleCharacter() {
        String genreName = "A";
        Genre genre = new Genre(genreName);
        
        assertNotNull(genre);
        assertEquals(genreName, genre.getGenre());
    }

    // Functional opaque-box test: Null genre throws exception
    @Test
    void testCreateGenreWithNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Genre(null)
        );
    }

    // Functional opaque-box test: Blank genre throws exception
    @Test
    void testCreateGenreWithBlankStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Genre("")
        );
    }

    // Functional opaque-box test: Genre with only spaces throws exception
    @Test
    void testCreateGenreWithOnlySpacesThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Genre("   ")
        );
    }

    // Functional opaque-box test: Genre exceeding maximum length throws exception
    @Test
    void testCreateGenreExceedingMaxLengthThrowsException() {
        String genreName = "A".repeat(101);
        
        assertThrows(IllegalArgumentException.class, () -> 
            new Genre(genreName)
        );
    }

    // Functional opaque-box test: Genre with special characters
    @Test
    void testCreateGenreWithSpecialCharacters() {
        String genreName = "Science-Fiction & Fantasy";
        Genre genre = new Genre(genreName);
        
        assertNotNull(genre);
        assertEquals(genreName, genre.getGenre());
    }

    // Functional opaque-box test: Genre with numbers
    @Test
    void testCreateGenreWithNumbers() {
        String genreName = "21st Century Literature";
        Genre genre = new Genre(genreName);
        
        assertNotNull(genre);
        assertEquals(genreName, genre.getGenre());
    }

    // Functional opaque-box test: Genre with unicode characters
    @Test
    void testCreateGenreWithUnicodeCharacters() {
        String genreName = "Ficção Científica";
        Genre genre = new Genre(genreName);
        
        assertNotNull(genre);
        assertEquals(genreName, genre.getGenre());
    }

    // Functional opaque-box test: Genre toString returns correct value
    @Test
    void testGenreToStringReturnsCorrectValue() {
        String genreName = "Mystery";
        Genre genre = new Genre(genreName);
        
        assertEquals(genreName, genre.toString());
    }
}
