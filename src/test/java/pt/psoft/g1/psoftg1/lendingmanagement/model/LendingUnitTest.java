package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test - Functional opaque-box testing with SUT = Lending class
 * Tests the Lending domain class in isolation
 */
class LendingUnitTest {

    private Book validBook;
    private ReaderDetails validReaderDetails;
    private final int lendingDuration = 15;
    private final int fineValuePerDayInCents = 50;

    @BeforeEach
    void setUp() {
        // Setup valid book
        Genre genre = new Genre("Fiction");
        Author author = new Author("Test Author", "Bio", null);
        List<Author> authors = new ArrayList<>();
        authors.add(author);
        validBook = new Book("9782826012092", "Test Book", "Description", genre, authors, null);
        
        // Setup valid reader
        Reader reader = Reader.newReader("test@example.com", "Password123!", "Test Reader");
        validReaderDetails = new ReaderDetails(1, reader, "2000-01-01", "912345678",
            true, true, true, null, new ArrayList<>());
    }

    // Functional opaque-box test: Valid lending creation
    @Test
    void testCreateValidLending() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        
        assertNotNull(lending);
        assertEquals(validBook, lending.getBook());
        assertEquals(validReaderDetails, lending.getReaderDetails());
        assertEquals(LocalDate.now(), lending.getStartDate());
        assertEquals(LocalDate.now().plusDays(lendingDuration), lending.getLimitDate());
        assertNull(lending.getReturnedDate());
        assertEquals(fineValuePerDayInCents, lending.getFineValuePerDayInCents());
    }

    // Functional opaque-box test: Lending number format
    @Test
    void testLendingNumberFormat() {
        Lending lending = new Lending(validBook, validReaderDetails, 123, lendingDuration, fineValuePerDayInCents);
        
        String lendingNumber = lending.getLendingNumber();
        assertNotNull(lendingNumber);
        assertTrue(lendingNumber.contains("" + LocalDate.now().getYear()));
    }

    // Functional opaque-box test: Null book throws exception
    @Test
    void testCreateLendingWithNullBookThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Lending(null, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents)
        );
    }

    // Functional opaque-box test: Null reader throws exception
    @Test
    void testCreateLendingWithNullReaderThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new Lending(validBook, null, 1, lendingDuration, fineValuePerDayInCents)
        );
    }

    // Functional opaque-box test: Set returned with commentary
    @Test
    void testSetReturnedWithCommentary() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        long version = lending.getVersion();
        
        lending.setReturned(version, "Great book!");
        
        assertNotNull(lending.getReturnedDate());
        assertEquals(LocalDate.now(), lending.getReturnedDate());
    }

    // Functional opaque-box test: Set returned without commentary
    @Test
    void testSetReturnedWithoutCommentary() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        long version = lending.getVersion();
        
        lending.setReturned(version, null);
        
        assertNotNull(lending.getReturnedDate());
        assertEquals(LocalDate.now(), lending.getReturnedDate());
    }

    // Functional opaque-box test: Set returned twice throws exception
    @Test
    void testSetReturnedTwiceThrowsException() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        long version = lending.getVersion();
        
        lending.setReturned(version, null);
        
        assertThrows(IllegalArgumentException.class, () ->
            lending.setReturned(version, null)
        );
    }

    // Functional opaque-box test: Set returned with stale version throws exception
    @Test
    void testSetReturnedWithStaleVersionThrowsException() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        
        assertThrows(StaleObjectStateException.class, () ->
            lending.setReturned(999L, null)
        );
    }

    // Functional opaque-box test: Days delayed when not returned and not overdue
    @Test
    void testGetDaysDelayedWhenNotReturnedAndNotOverdue() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        
        int daysDelayed = lending.getDaysDelayed();
        
        assertEquals(0, daysDelayed);
    }

    // Functional opaque-box test: Days delayed when returned on time
    @Test
    void testGetDaysDelayedWhenReturnedOnTime() {
        Lending lending = Lending.newBootstrappingLending(
            validBook, validReaderDetails,
            LocalDate.now().getYear(), 1,
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(5),
            lendingDuration, fineValuePerDayInCents
        );
        
        int daysDelayed = lending.getDaysDelayed();
        
        assertEquals(0, daysDelayed);
    }

    // Functional opaque-box test: Days delayed when returned late
    @Test
    void testGetDaysDelayedWhenReturnedLate() {
        LocalDate startDate = LocalDate.now().minusDays(20);
        LocalDate returnedDate = LocalDate.now().minusDays(2);
        
        Lending lending = Lending.newBootstrappingLending(
            validBook, validReaderDetails,
            LocalDate.now().getYear(), 1,
            startDate,
            returnedDate,
            lendingDuration, fineValuePerDayInCents
        );
        
        int daysDelayed = lending.getDaysDelayed();
        
        assertTrue(daysDelayed > 0);
    }

    // Functional opaque-box test: Days until return when not returned
    @Test
    void testGetDaysUntilReturnWhenNotReturned() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        
        assertTrue(lending.getDaysUntilReturn().isPresent());
        assertEquals(lendingDuration, lending.getDaysUntilReturn().get());
    }

    // Functional opaque-box test: Days until return when already returned
    @Test
    void testGetDaysUntilReturnWhenAlreadyReturned() {
        Lending lending = Lending.newBootstrappingLending(
            validBook, validReaderDetails,
            LocalDate.now().getYear(), 1,
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(5),
            lendingDuration, fineValuePerDayInCents
        );
        
        assertFalse(lending.getDaysUntilReturn().isPresent());
    }

    // Functional opaque-box test: Days overdue when not overdue
    @Test
    void testGetDaysOverdueWhenNotOverdue() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        
        assertFalse(lending.getDaysOverdue().isPresent());
    }

    // Functional opaque-box test: Days overdue when overdue
    @Test
    void testGetDaysOverdueWhenOverdue() {
        LocalDate startDate = LocalDate.now().minusDays(20);
        
        Lending lending = Lending.newBootstrappingLending(
            validBook, validReaderDetails,
            LocalDate.now().getYear(), 1,
            startDate,
            null,
            lendingDuration, fineValuePerDayInCents
        );
        
        assertTrue(lending.getDaysOverdue().isPresent());
        assertTrue(lending.getDaysOverdue().get() > 0);
    }

    // Functional opaque-box test: Fine value when no delay
    @Test
    void testGetFineValueWhenNoDelay() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        
        assertFalse(lending.getFineValueInCents().isPresent());
    }

    // Functional opaque-box test: Fine value when delayed
    @Test
    void testGetFineValueWhenDelayed() {
        LocalDate startDate = LocalDate.now().minusDays(20);
        
        Lending lending = Lending.newBootstrappingLending(
            validBook, validReaderDetails,
            LocalDate.now().getYear(), 1,
            startDate,
            null,
            lendingDuration, fineValuePerDayInCents
        );
        
        int daysDelayed = lending.getDaysDelayed();
        if (daysDelayed > 0) {
            assertTrue(lending.getFineValueInCents().isPresent());
            assertEquals(fineValuePerDayInCents * daysDelayed, lending.getFineValueInCents().get());
        }
    }

    // Functional opaque-box test: Get title returns book title
    @Test
    void testGetTitleReturnsBookTitle() {
        Lending lending = new Lending(validBook, validReaderDetails, 1, lendingDuration, fineValuePerDayInCents);
        
        assertEquals("Test Book", lending.getTitle());
    }

    // Functional opaque-box test: Bootstrapping lending with specific dates
    @Test
    void testBootstrappingLendingWithSpecificDates() {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate returnedDate = LocalDate.of(2023, 1, 10);
        
        Lending lending = Lending.newBootstrappingLending(
            validBook, validReaderDetails,
            2023, 100,
            startDate, returnedDate,
            lendingDuration, fineValuePerDayInCents
        );
        
        assertNotNull(lending);
        assertEquals(startDate, lending.getStartDate());
        assertEquals(returnedDate, lending.getReturnedDate());
        assertEquals(startDate.plusDays(lendingDuration), lending.getLimitDate());
    }
}
