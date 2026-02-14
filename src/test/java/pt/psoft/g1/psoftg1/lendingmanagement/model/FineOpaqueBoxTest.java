package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Functional Opaque-Box Tests for Fine domain class
 * 
 * Test Strategy: Black-box testing approach without knowledge of internal implementation
 * SUT: Fine class
 * 
 * Tests fine creation, validation, and calculations from external perspective
 */
@DisplayName("Fine Opaque-Box Tests")
class FineOpaqueBoxTest {

    private Lending overdueLending;
    private Book testBook;
    private ReaderDetails testReaderDetails;
    private Reader testReader;

    @BeforeEach
    void setUp() {
        // Create test data
        Genre genre = new Genre("Fiction");
        Author author = new Author("Test Author", "Bio", null);
        testBook = new Book("9783161484100", "Test Book", "Description",
                genre, List.of(author), null);

        testReader = Reader.newReader("test@example.com", "Password123!", "Test Reader");
        testReaderDetails = new ReaderDetails(1, testReader, "2000-01-01", "912345678",
                true, true, true, null, new ArrayList<>());

        // Create an overdue lending (by using bootstrapping method with past dates)
        overdueLending = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                1,
                LocalDate.now().minusDays(20),  // Started 20 days ago
                null,                            // Not returned yet
                15,                              // 15 days lending duration
                50                               // 50 cents per day fine
        );
    }

    @Test
    @DisplayName("Opaque-Box: Should create fine for overdue lending")
    void testCreateFineForOverdueLending() {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert
        assertNotNull(fine, "Fine should be created");
        assertNotNull(fine.getLending(), "Fine should have associated lending");
        assertEquals(overdueLending, fine.getLending(), "Fine should reference the correct lending");
    }

    @Test
    @DisplayName("Opaque-Box: Should calculate correct fine value for overdue lending")
    void testCalculateCorrectFineValue() {
        // Arrange
        int expectedDaysDelayed = overdueLending.getDaysDelayed();
        int expectedFineValue = 50 * expectedDaysDelayed; // 50 cents per day

        // Act
        Fine fine = new Fine(overdueLending);

        // Assert
        assertTrue(expectedDaysDelayed > 0, "Lending should be overdue");
        assertEquals(expectedFineValue, fine.getCentsValue(), "Fine value should match days delayed * rate");
    }

    @Test
    @DisplayName("Opaque-Box: Should store fine value per day from lending")
    void testStoreFineValuePerDay() {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert
        assertEquals(50, fine.getFineValuePerDayInCents(), "Fine should store the daily rate");
    }

    @Test
    @DisplayName("Opaque-Box: Should throw exception for non-overdue lending")
    void testThrowExceptionForNonOverdueLending() {
        // Arrange - Create a lending that is not overdue
        Lending currentLending = new Lending(testBook, testReaderDetails, 2, 15, 50);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Fine(currentLending),
                "Should throw exception when lending is not overdue");
    }

    @Test
    @DisplayName("Opaque-Box: Should throw exception for null lending")
    void testThrowExceptionForNullLending() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new Fine(null),
                "Should throw exception when lending is null");
    }

    @Test
    @DisplayName("Opaque-Box: Should calculate fine for 1 day overdue")
    void testCalculateFineForOneDayOverdue() {
        // Arrange - Create lending that is exactly 1 day overdue
        Lending oneDayOverdue = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                3,
                LocalDate.now().minusDays(16), // Started 16 days ago
                null,                           // Not returned
                15,                             // 15 days duration = 1 day overdue
                50
        );

        // Act
        Fine fine = new Fine(oneDayOverdue);

        // Assert
        assertEquals(50, fine.getCentsValue(), "Fine for 1 day should be 50 cents");
    }

    @Test
    @DisplayName("Opaque-Box: Should calculate fine for multiple days overdue")
    void testCalculateFineForMultipleDaysOverdue() {
        // Arrange - Create lending that is 10 days overdue
        Lending tenDaysOverdue = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                4,
                LocalDate.now().minusDays(25), // Started 25 days ago
                null,                           // Not returned
                15,                             // 15 days duration = 10 days overdue
                50
        );

        // Act
        Fine fine = new Fine(tenDaysOverdue);

        // Assert
        assertEquals(500, fine.getCentsValue(), "Fine for 10 days should be 500 cents");
    }

    @Test
    @DisplayName("Opaque-Box: Should calculate fine with different daily rates")
    void testCalculateFineWithDifferentDailyRates() {
        // Arrange - Create lending with different fine rate
        Lending customRateLending = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                5,
                LocalDate.now().minusDays(20), // Started 20 days ago
                null,                           // Not returned
                15,                             // 15 days duration = 5 days overdue
                100                             // 100 cents per day (different rate)
        );

        // Act
        Fine fine = new Fine(customRateLending);

        // Assert
        assertEquals(100, fine.getFineValuePerDayInCents(), "Should use custom daily rate");
        assertEquals(500, fine.getCentsValue(), "Fine should be 5 days * 100 cents");
    }

    @Test
    @DisplayName("Opaque-Box: Should handle returned overdue lending")
    void testHandleReturnedOverdueLending() {
        // Arrange - Create a returned overdue lending
        Lending returnedOverdue = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                6,
                LocalDate.now().minusDays(20),      // Started 20 days ago
                LocalDate.now().minusDays(2),       // Returned 2 days ago (18 days after start = 3 days late)
                15,                                  // 15 days duration
                50
        );

        // Act
        Fine fine = new Fine(returnedOverdue);

        // Assert
        assertNotNull(fine, "Fine should be created for returned overdue lending");
        assertEquals(150, fine.getCentsValue(), "Fine should be calculated based on actual return date");
    }

    @Test
    @DisplayName("Opaque-Box: Fine should be immutable after creation")
    void testFineImmutableAfterCreation() {
        // Act
        Fine fine = new Fine(overdueLending);
        int originalValue = fine.getCentsValue();
        int originalDailyRate = fine.getFineValuePerDayInCents();

        // Wait a moment and verify values don't change
        // Assert
        assertEquals(originalValue, fine.getCentsValue(), "Fine value should not change");
        assertEquals(originalDailyRate, fine.getFineValuePerDayInCents(), "Daily rate should not change");
    }

    @Test
    @DisplayName("Opaque-Box: Should calculate fine correctly at boundary (exactly on due date)")
    void testFineCalculationAtBoundary() {
        // Arrange - Create lending that is returned exactly on due date
        Lending onTimeLending = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                7,
                LocalDate.now().minusDays(15),  // Started 15 days ago
                LocalDate.now(),                // Returned today (exactly on time)
                15,                             // 15 days duration
                50
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Fine(onTimeLending),
                "Should not create fine for lending returned on time");
    }

    @Test
    @DisplayName("Opaque-Box: Should maintain reference to original lending")
    void testMaintainReferenceToOriginalLending() {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert
        assertSame(overdueLending, fine.getLending(), "Fine should maintain reference to same lending instance");
        assertEquals(overdueLending.getLendingNumber(), fine.getLending().getLendingNumber(),
                "Fine should reference lending with correct lending number");
    }

    @Test
    @DisplayName("Opaque-Box: Should calculate fine value as positive number")
    void testFineValueIsPositive() {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert
        assertTrue(fine.getCentsValue() > 0, "Fine value should always be positive");
        assertTrue(fine.getFineValuePerDayInCents() >= 0, "Daily fine rate should be non-negative");
    }

    @Test
    @DisplayName("Opaque-Box: Should handle zero cents per day rate")
    void testHandleZeroCentsPerDayRate() {
        // Arrange - Create overdue lending with zero fine rate
        Lending zeroRateLending = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                8,
                LocalDate.now().minusDays(20),
                null,
                15,
                0  // Zero cents per day
        );

        // Act
        Fine fine = new Fine(zeroRateLending);

        // Assert
        assertEquals(0, fine.getCentsValue(), "Fine value should be zero when rate is zero");
        assertEquals(0, fine.getFineValuePerDayInCents(), "Daily rate should be zero");
    }
}
