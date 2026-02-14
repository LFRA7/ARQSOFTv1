package pt.psoft.g1.psoftg1.lendingmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *  Service Unit Test
 * 
 * Unit tests for LendingServiceImpl with mocked dependencies.
 * Tests the lending service layer business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
class LendingServiceImplUnitTest {

    @Mock
    private LendingRepository lendingRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReaderRepository readerRepository;

    @InjectMocks
    private LendingServiceImpl lendingService;

    private Book testBook;
    private ReaderDetails testReader;
    private Lending testLending;
    private Genre testGenre;
    private Author testAuthor;
    private final String validIsbn = "9782826012092";
    private final String validReaderNumber = "2024/1";
    private final String validLendingNumber = "2024/1";

    @BeforeEach
    void setUp() {
        // Set configuration properties using ReflectionTestUtils
        ReflectionTestUtils.setField(lendingService, "lendingDurationInDays", 15);
        ReflectionTestUtils.setField(lendingService, "fineValuePerDayInCents", 50);

        // Create test entities
        testAuthor = new Author("Test Author", "Test Bio", null);
        testGenre = new Genre("Fiction");
        testBook = new Book(validIsbn, "Test Book", "Description", testGenre, List.of(testAuthor), null);
        
        Reader reader = Reader.newReader("test@email.com", "Password123!", "Test Reader");
        testReader = new ReaderDetails(1, reader, "2000-01-01", "912345678", true, true, true, null, null);
        
        testLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 1, 
            LocalDate.of(2024, 1, 1), 
            null, 15, 50
        );
    }

    /**
     *  Service Unit Test
     * Tests finding a lending by lending number successfully
     */
    @Test
    void testFindByLendingNumber_Success() {
        when(lendingRepository.findByLendingNumber(validLendingNumber)).thenReturn(Optional.of(testLending));

        Optional<Lending> result = lendingService.findByLendingNumber(validLendingNumber);

        assertTrue(result.isPresent());
        assertEquals(testLending, result.get());
        verify(lendingRepository).findByLendingNumber(validLendingNumber);
    }

    /**
     *  Service Unit Test
     * Tests finding a lending by lending number when not found
     */
    @Test
    void testFindByLendingNumber_NotFound() {
        when(lendingRepository.findByLendingNumber(validLendingNumber)).thenReturn(Optional.empty());

        Optional<Lending> result = lendingService.findByLendingNumber(validLendingNumber);

        assertFalse(result.isPresent());
        verify(lendingRepository).findByLendingNumber(validLendingNumber);
    }

    /**
     *  Service Unit Test
     * Tests listing lendings by reader number and ISBN without filter
     */
    @Test
    void testListByReaderNumberAndIsbn_NoFilter() {
        List<Lending> lendings = List.of(testLending);
        when(lendingRepository.listByReaderNumberAndIsbn(validReaderNumber, validIsbn))
            .thenReturn(lendings);

        List<Lending> result = lendingService.listByReaderNumberAndIsbn(
            validReaderNumber, validIsbn, Optional.empty()
        );

        assertEquals(1, result.size());
        verify(lendingRepository).listByReaderNumberAndIsbn(validReaderNumber, validIsbn);
    }

    /**
     *  Service Unit Test
     * Tests listing lendings filtered by returned status (true)
     */
    @Test
    void testListByReaderNumberAndIsbn_FilterReturned() {
        Lending returnedLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 2, 
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 10), 
            15, 50
        );
        Lending notReturnedLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 3, 
            LocalDate.of(2024, 2, 1), 
            null, 
            15, 50
        );
        
        List<Lending> lendings = new ArrayList<>(List.of(returnedLending, notReturnedLending));
        when(lendingRepository.listByReaderNumberAndIsbn(validReaderNumber, validIsbn))
            .thenReturn(lendings);

        List<Lending> result = lendingService.listByReaderNumberAndIsbn(
            validReaderNumber, validIsbn, Optional.of(true)
        );

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getReturnedDate());
        verify(lendingRepository).listByReaderNumberAndIsbn(validReaderNumber, validIsbn);
    }

    /**
     *  Service Unit Test
     * Tests listing lendings filtered by not returned status (false)
     */
    @Test
    void testListByReaderNumberAndIsbn_FilterNotReturned() {
        Lending returnedLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 2, 
            LocalDate.of(2024, 1, 1), 
            LocalDate.of(2024, 1, 10), 
            15, 50
        );
        Lending notReturnedLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 3, 
            LocalDate.of(2024, 2, 1), 
            null, 
            15, 50
        );
        
        List<Lending> lendings = new ArrayList<>(List.of(returnedLending, notReturnedLending));
        when(lendingRepository.listByReaderNumberAndIsbn(validReaderNumber, validIsbn))
            .thenReturn(lendings);

        List<Lending> result = lendingService.listByReaderNumberAndIsbn(
            validReaderNumber, validIsbn, Optional.of(false)
        );

        assertEquals(1, result.size());
        assertNull(result.get(0).getReturnedDate());
        verify(lendingRepository).listByReaderNumberAndIsbn(validReaderNumber, validIsbn);
    }

    /**
     *  Service Unit Test
     * Tests creating a lending successfully
     */
    @Test
    void testCreate_Success() {
        CreateLendingRequest request = new CreateLendingRequest(validIsbn, validReaderNumber);
        
        when(lendingRepository.listOutstandingByReaderNumber(validReaderNumber))
            .thenReturn(new ArrayList<>());
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.of(testBook));
        when(readerRepository.findByReaderNumber(validReaderNumber)).thenReturn(Optional.of(testReader));
        when(lendingRepository.getCountFromCurrentYear()).thenReturn(0);
        when(lendingRepository.save(any(Lending.class))).thenReturn(testLending);

        Lending result = lendingService.create(request);

        assertNotNull(result);
        verify(lendingRepository).listOutstandingByReaderNumber(validReaderNumber);
        verify(bookRepository).findByIsbn(validIsbn);
        verify(readerRepository).findByReaderNumber(validReaderNumber);
        verify(lendingRepository).save(any(Lending.class));
    }

    /**
     *  Service Unit Test
     * Tests creating a lending when reader has overdue books
     */
    @Test
    void testCreate_ReaderHasOverdueBooks_ThrowsException() {
        CreateLendingRequest request = new CreateLendingRequest(validIsbn, validReaderNumber);
        
        // Create an overdue lending
        Lending overdueLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 1, 
            LocalDate.now().minusDays(30), 
            null, 
            15, 50
        );
        
        when(lendingRepository.listOutstandingByReaderNumber(validReaderNumber))
            .thenReturn(List.of(overdueLending));

        assertThrows(LendingForbiddenException.class, () -> lendingService.create(request));
        
        verify(lendingRepository).listOutstandingByReaderNumber(validReaderNumber);
        verify(bookRepository, never()).findByIsbn(any());
        verify(lendingRepository, never()).save(any());
    }

    /**
     *  Service Unit Test
     * Tests creating a lending when reader already has 3 outstanding books
     */
    @Test
    void testCreate_ReaderHasThreeOutstandingBooks_ThrowsException() {
        CreateLendingRequest request = new CreateLendingRequest(validIsbn, validReaderNumber);
        
        // Create 3 current lendings (not overdue)
        Lending lending1 = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 1, 
            LocalDate.now().minusDays(5), 
            null, 
            15, 50
        );
        Lending lending2 = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 2, 
            LocalDate.now().minusDays(3), 
            null, 
            15, 50
        );
        Lending lending3 = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 3, 
            LocalDate.now().minusDays(1), 
            null, 
            15, 50
        );
        
        when(lendingRepository.listOutstandingByReaderNumber(validReaderNumber))
            .thenReturn(List.of(lending1, lending2, lending3));

        assertThrows(LendingForbiddenException.class, () -> lendingService.create(request));
        
        verify(lendingRepository).listOutstandingByReaderNumber(validReaderNumber);
        verify(bookRepository, never()).findByIsbn(any());
        verify(lendingRepository, never()).save(any());
    }

    /**
     *  Service Unit Test
     * Tests creating a lending when book is not found
     */
    @Test
    void testCreate_BookNotFound_ThrowsException() {
        CreateLendingRequest request = new CreateLendingRequest(validIsbn, validReaderNumber);
        
        when(lendingRepository.listOutstandingByReaderNumber(validReaderNumber))
            .thenReturn(new ArrayList<>());
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> lendingService.create(request));
        
        verify(bookRepository).findByIsbn(validIsbn);
        verify(lendingRepository, never()).save(any());
    }

    /**
     *  Service Unit Test
     * Tests creating a lending when reader is not found
     */
    @Test
    void testCreate_ReaderNotFound_ThrowsException() {
        CreateLendingRequest request = new CreateLendingRequest(validIsbn, validReaderNumber);
        
        when(lendingRepository.listOutstandingByReaderNumber(validReaderNumber))
            .thenReturn(new ArrayList<>());
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.of(testBook));
        when(readerRepository.findByReaderNumber(validReaderNumber)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> lendingService.create(request));
        
        verify(readerRepository).findByReaderNumber(validReaderNumber);
        verify(lendingRepository, never()).save(any());
    }

    /**
     *  Service Unit Test
     * Tests setting a lending as returned when it's on time
     */
    @Test
    void testSetReturned_OnTime_NoFine() {
        SetLendingReturnedRequest request = new SetLendingReturnedRequest("Returned in good condition");
        
        // Create a lending that is not overdue
        Lending onTimeLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 1, 
            LocalDate.now().minusDays(5), 
            null, 
            15, 50
        );
        
        when(lendingRepository.findByLendingNumber(validLendingNumber))
            .thenReturn(Optional.of(onTimeLending));
        when(lendingRepository.save(any(Lending.class))).thenReturn(onTimeLending);

        Lending result = lendingService.setReturned(validLendingNumber, request, 0L);

        assertNotNull(result);
        verify(lendingRepository).findByLendingNumber(validLendingNumber);
        verify(lendingRepository).save(onTimeLending);
        verify(fineRepository, never()).save(any());
    }

    /**
     *  Service Unit Test
     * Tests setting a lending as returned when it's overdue (creates fine)
     */
    @Test
    void testSetReturned_Overdue_CreatesFine() {
        SetLendingReturnedRequest request = new SetLendingReturnedRequest("Returned late");
        
        // Create an overdue lending
        Lending overdueLending = Lending.newBootstrappingLending(
            testBook, testReader, 2024, 1, 
            LocalDate.now().minusDays(30), 
            null, 
            15, 50
        );
        
        when(lendingRepository.findByLendingNumber(validLendingNumber))
            .thenReturn(Optional.of(overdueLending));
        when(lendingRepository.save(any(Lending.class))).thenReturn(overdueLending);

        Lending result = lendingService.setReturned(validLendingNumber, request, 0L);

        assertNotNull(result);
        verify(lendingRepository).findByLendingNumber(validLendingNumber);
        verify(lendingRepository).save(overdueLending);
        verify(fineRepository).save(any());
    }

    /**
     *  Service Unit Test
     * Tests setting a lending as returned when lending is not found
     */
    @Test
    void testSetReturned_LendingNotFound_ThrowsException() {
        SetLendingReturnedRequest request = new SetLendingReturnedRequest("Commentary");
        
        when(lendingRepository.findByLendingNumber(validLendingNumber))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, 
            () -> lendingService.setReturned(validLendingNumber, request, 0L));
        
        verify(lendingRepository).findByLendingNumber(validLendingNumber);
        verify(lendingRepository, never()).save(any());
        verify(fineRepository, never()).save(any());
    }

    /**
     *  Service Unit Test
     * Tests getting average lending duration
     */
    @Test
    void testGetAverageDuration_Success() {
        when(lendingRepository.getAverageDuration()).thenReturn(12.5678);

        Double result = lendingService.getAverageDuration();

        assertEquals(12.6, result);
        verify(lendingRepository).getAverageDuration();
    }

    /**
     *  Service Unit Test
     * Tests getting average lending duration with exact value
     */
    @Test
    void testGetAverageDuration_ExactValue() {
        when(lendingRepository.getAverageDuration()).thenReturn(10.0);

        Double result = lendingService.getAverageDuration();

        assertEquals(10.0, result);
        verify(lendingRepository).getAverageDuration();
    }

    /**
     *  Service Unit Test
     * Tests getting overdue lendings with default pagination
     */
    @Test
    void testGetOverdue_WithDefaultPage() {
        List<Lending> overdueLendings = List.of(testLending);
        when(lendingRepository.getOverdue(any(Page.class))).thenReturn(overdueLendings);

        List<Lending> result = lendingService.getOverdue(null);

        assertEquals(1, result.size());
        verify(lendingRepository).getOverdue(any(Page.class));
    }

    /**
     *  Service Unit Test
     * Tests getting overdue lendings with custom pagination
     */
    @Test
    void testGetOverdue_WithCustomPage() {
        Page customPage = new Page(2, 20);
        List<Lending> overdueLendings = List.of(testLending);
        when(lendingRepository.getOverdue(customPage)).thenReturn(overdueLendings);

        List<Lending> result = lendingService.getOverdue(customPage);

        assertEquals(1, result.size());
        verify(lendingRepository).getOverdue(customPage);
    }

    /**
     *  Service Unit Test
     * Tests getting average lending duration by ISBN
     */
    @Test
    void testGetAvgLendingDurationByIsbn_Success() {
        when(lendingRepository.getAvgLendingDurationByIsbn(validIsbn)).thenReturn(14.3456);

        Double result = lendingService.getAvgLendingDurationByIsbn(validIsbn);

        assertEquals(14.3, result);
        verify(lendingRepository).getAvgLendingDurationByIsbn(validIsbn);
    }

    /**
     *  Service Unit Test
     * Tests searching lendings with default parameters
     */
    @Test
    void testSearchLendings_WithDefaultParameters() {
        List<Lending> lendings = List.of(testLending);
        when(lendingRepository.searchLendings(any(Page.class), any(), any(), any(), any(), any()))
            .thenReturn(lendings);

        List<Lending> result = lendingService.searchLendings(null, null);

        assertEquals(1, result.size());
        verify(lendingRepository).searchLendings(any(Page.class), any(), any(), any(), any(), any());
    }

    /**
     *  Service Unit Test
     * Tests searching lendings with custom query
     */
    @Test
    void testSearchLendings_WithCustomQuery() {
        Page page = new Page(1, 10);
        SearchLendingQuery query = new SearchLendingQuery(
            validReaderNumber, 
            validIsbn, 
            true, 
            "2024-01-01", 
            "2024-12-31"
        );
        List<Lending> lendings = List.of(testLending);
        
        when(lendingRepository.searchLendings(
            eq(page), 
            eq(validReaderNumber), 
            eq(validIsbn), 
            eq(true), 
            eq(LocalDate.of(2024, 1, 1)), 
            eq(LocalDate.of(2024, 12, 31))
        )).thenReturn(lendings);

        List<Lending> result = lendingService.searchLendings(page, query);

        assertEquals(1, result.size());
        verify(lendingRepository).searchLendings(
            eq(page), 
            eq(validReaderNumber), 
            eq(validIsbn), 
            eq(true), 
            any(LocalDate.class), 
            any(LocalDate.class)
        );
    }

    /**
     *  Service Unit Test
     * Tests searching lendings with invalid date format throws exception
     */
    @Test
    void testSearchLendings_InvalidDateFormat_ThrowsException() {
        SearchLendingQuery query = new SearchLendingQuery(
            validReaderNumber, 
            validIsbn, 
            true, 
            "invalid-date", 
            "2024-12-31"
        );

        assertThrows(IllegalArgumentException.class, 
            () -> lendingService.searchLendings(null, query));
    }

    /**
     *  Service Unit Test
     * Tests searching lendings with only start date
     */
    @Test
    void testSearchLendings_OnlyStartDate() {
        SearchLendingQuery query = new SearchLendingQuery(
            "", 
            "", 
            null, 
            "2024-01-01", 
            null
        );
        List<Lending> lendings = List.of(testLending);
        
        when(lendingRepository.searchLendings(any(Page.class), any(), any(), any(), any(), any()))
            .thenReturn(lendings);

        List<Lending> result = lendingService.searchLendings(null, query);

        assertEquals(1, result.size());
        verify(lendingRepository).searchLendings(
            any(Page.class), 
            eq(""), 
            eq(""), 
            isNull(), 
            eq(LocalDate.of(2024, 1, 1)), 
            isNull()
        );
    }
}
