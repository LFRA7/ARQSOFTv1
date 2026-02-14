package pt.psoft.g1.psoftg1.genremanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Service Layer Unit Tests for GenreServiceImpl
 * Tests business logic for genre management
 */
@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    private Genre genre;

    @BeforeEach
    void setUp() {
        genre = new Genre("Fiction");
    }

    /**
     *  Service Unit Test
     * Tests successful retrieval of genre by string name
     */
    @Test
    void testFindByString_Success() {
        
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(genre));

        
        Optional<Genre> result = genreService.findByString("Fiction");

        
        assertTrue(result.isPresent());
        assertEquals("Fiction", result.get().getGenre());
        verify(genreRepository).findByString("Fiction");
    }

    /**
     *  Service Unit Test
     * Tests that empty optional is returned when genre not found
     */
    @Test
    void testFindByString_NotFound() {
        
        when(genreRepository.findByString("NonExistent")).thenReturn(Optional.empty());

        
        Optional<Genre> result = genreService.findByString("NonExistent");

        
        assertFalse(result.isPresent());
        verify(genreRepository).findByString("NonExistent");
    }

    /**
     *  Service Unit Test
     * Tests repository interaction is called exactly once
     */
    @Test
    void testFindByString_VerifySingleRepositoryCall() {
        
        when(genreRepository.findByString(anyString())).thenReturn(Optional.of(genre));

        
        genreService.findByString("Fiction");

        
        verify(genreRepository, times(1)).findByString("Fiction");
        verifyNoMoreInteractions(genreRepository);
    }

    /**
     *  Service Unit Test
     * Tests findAll returns all genres from repository
     */
    @Test
    void testFindAll_Success() {
        
        List<Genre> genres = new ArrayList<>();
        genres.add(genre);
        genres.add(new Genre("Science"));
        when(genreRepository.findAll()).thenReturn(genres);

        
        Iterable<Genre> result = genreService.findAll();

        
        assertNotNull(result);
        assertEquals(2, ((List<Genre>) result).size());
        verify(genreRepository).findAll();
    }

    /**
     *  Service Unit Test
     * Tests successful save of genre
     */
    @Test
    void testSave_Success() {
        
        when(genreRepository.save(genre)).thenReturn(genre);

        
        Genre result = genreService.save(genre);

        
        assertNotNull(result);
        assertEquals("Fiction", result.getGenre());
        verify(genreRepository).save(genre);
    }

    /**
     *  Service Unit Test
     * Tests that save is delegated to repository
     */
    @Test
    void testSave_VerifyRepositoryInteraction() {
        
        Genre newGenre = new Genre("Mystery");
        when(genreRepository.save(newGenre)).thenReturn(newGenre);

        
        genreService.save(newGenre);

        
        verify(genreRepository, times(1)).save(newGenre);
        verifyNoMoreInteractions(genreRepository);
    }
}
