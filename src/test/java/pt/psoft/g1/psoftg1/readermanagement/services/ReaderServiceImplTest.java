package pt.psoft.g1.psoftg1.readermanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDate;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumber;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Service Layer Unit Tests for ReaderServiceImpl
 * Tests business logic for reader management
 */
@ExtendWith(MockitoExtension.class)
class ReaderServiceImplTest {

    @Mock
    private ReaderRepository readerRepository;

    @InjectMocks
    private ReaderServiceImpl readerService;

    private ReaderDetails readerDetails;
    private Reader reader;

    @BeforeEach
    void setUp() {
        reader = Reader.newReader("reader@test.com", "Password123!", "John Doe");
        readerDetails = new ReaderDetails(
                1, reader, "2000-01-01", "912345678",
                true, true, true, null, new ArrayList<>()
        );
    }

    /**
     *  Service Unit Test
     * Tests successful retrieval of reader by reader number
     */
    @Test
    void testFindByReaderNumber_Success() {
        
        when(readerRepository.findByReaderNumber("2024/1")).thenReturn(Optional.of(readerDetails));

        
        Optional<ReaderDetails> result = readerService.findByReaderNumber("2024/1");

        
        assertTrue(result.isPresent());
        assertNotNull(result.get());
        verify(readerRepository).findByReaderNumber("2024/1");
    }

    /**
     *  Service Unit Test
     * Tests that empty optional is returned when reader not found
     */
    @Test
    void testFindByReaderNumber_NotFound() {
        
        when(readerRepository.findByReaderNumber("9999/999")).thenReturn(Optional.empty());

        
        Optional<ReaderDetails> result = readerService.findByReaderNumber("9999/999");

        
        assertFalse(result.isPresent());
        verify(readerRepository).findByReaderNumber("9999/999");
    }

    /**
     *  Service Unit Test
     * Tests successful retrieval of reader by username
     */
    @Test
    void testFindByUsername_Success() {
        
        when(readerRepository.findByUsername("reader@test.com")).thenReturn(Optional.of(readerDetails));

        
        Optional<ReaderDetails> result = readerService.findByUsername("reader@test.com");

        
        assertTrue(result.isPresent());
        assertNotNull(result.get());
        verify(readerRepository).findByUsername("reader@test.com");
    }

    /**
     *  Service Unit Test
     * Tests that empty optional is returned when username not found
     */
    @Test
    void testFindByUsername_NotFound() {
        
        when(readerRepository.findByUsername("nonexistent@test.com")).thenReturn(Optional.empty());

        
        Optional<ReaderDetails> result = readerService.findByUsername("nonexistent@test.com");

        
        assertFalse(result.isPresent());
        verify(readerRepository).findByUsername("nonexistent@test.com");
    }

    /**
     *  Service Unit Test
     * Tests successful retrieval of readers by phone number
     */
    @Test
    void testFindByPhoneNumber_Success() {
        
        List<ReaderDetails> readers = new ArrayList<>();
        readers.add(readerDetails);
        when(readerRepository.findByPhoneNumber(anyString())).thenReturn(readers);

        
        List<ReaderDetails> result = readerService.findByPhoneNumber("912345678");

        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(readerRepository).findByPhoneNumber("912345678");
    }

    /**
     *  Service Unit Test
     * Tests that empty list is returned when no readers found with phone number
     */
    @Test
    void testFindByPhoneNumber_EmptyList() {
        
        when(readerRepository.findByPhoneNumber("999999999")).thenReturn(new ArrayList<>());

        
        List<ReaderDetails> result = readerService.findByPhoneNumber("999999999");

        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(readerRepository).findByPhoneNumber("999999999");
    }

    /**
     *  Service Unit Test
     * Tests successful retrieval of all readers
     */
    @Test
    void testFindAll_Success() {
        
        List<ReaderDetails> readers = new ArrayList<>();
        readers.add(readerDetails);
        when(readerRepository.findAll()).thenReturn(readers);

        
        Iterable<ReaderDetails> result = readerService.findAll();

        
        assertNotNull(result);
        assertEquals(1, ((List<ReaderDetails>) result).size());
        verify(readerRepository).findAll();
    }

    /**
     *  Service Unit Test
     * Tests that repository interaction happens exactly once
     */
    @Test
    void testFindByReaderNumber_VerifySingleRepositoryCall() {
        
        when(readerRepository.findByReaderNumber(anyString())).thenReturn(Optional.of(readerDetails));

        
        readerService.findByReaderNumber("2024/1");

        
        verify(readerRepository, times(1)).findByReaderNumber("2024/1");
        verifyNoMoreInteractions(readerRepository);
    }
}
