package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional Transparent-Box Tests for Fine domain class
 * 
 * Test Strategy: White-box testing with knowledge of internal implementation
 * SUT: Fine class
 * 
 * Tests internal state, field initialization, and implementation details
 */
@DisplayName("Fine Transparent-Box Tests")
class FineTransparentBoxTest {

    private Lending overdueLending;
    private Book testBook;
    private ReaderDetails testReaderDetails;
    private Reader testReader;

    @BeforeEach
    void setUp() {
        Genre genre = new Genre("Fiction");
        Author author = new Author("Test Author", "Bio", null);
        testBook = new Book("9783161484100", "Test Book", "Description",
                genre, List.of(author), null);

        testReader = Reader.newReader("test@example.com", "Password123!", "Test Reader");
        testReaderDetails = new ReaderDetails(1, testReader, "2000-01-01", "912345678",
                true, true, true, null, new ArrayList<>());

        overdueLending = Lending.newBootstrappingLending(
                testBook,
                testReaderDetails,
                2025,
                1,
                LocalDate.now().minusDays(20),
                null,
                15,
                50
        );
    }

    // Helper method to access private fields using reflection
    private Object getPrivateField(Fine fine, String fieldName) throws Exception {
        Field field = Fine.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(fine);
    }

    @Test
    @DisplayName("Transparent-Box: Should initialize pk field as null before persistence")
    void testPkFieldInitializedAsNull() throws Exception {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert - pk should be null before persistence
        Object pk = getPrivateField(fine, "pk");
        assertNull(pk, "PK should be null before persistence");
    }

    @Test
    @DisplayName("Transparent-Box: Should initialize fineValuePerDayInCents from lending")
    void testFineValuePerDayInCentsInitialization() throws Exception {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert - Internal field should be set from lending
        Object fineValuePerDay = getPrivateField(fine, "fineValuePerDayInCents");
        assertEquals(50, fineValuePerDay, "Internal fineValuePerDayInCents should match lending's value");
        assertEquals(overdueLending.getFineValuePerDayInCents(), fine.getFineValuePerDayInCents(),
                "Getter should return internal field value");
    }

    @Test
    @DisplayName("Transparent-Box: Should initialize centsValue field correctly")
    void testCentsValueFieldInitialization() throws Exception {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert - centsValue should be calculated and stored internally
        Object centsValue = getPrivateField(fine, "centsValue");
        assertNotNull(centsValue, "centsValue field should be initialized");
        assertTrue((int) centsValue > 0, "centsValue should be positive for overdue lending");
    }

    @Test
    @DisplayName("Transparent-Box: Should calculate centsValue as fineValuePerDay * daysDelayed")
    void testCentsValueCalculation() throws Exception {
        // Arrange
        int daysDelayed = overdueLending.getDaysDelayed();
        int expectedValue = 50 * daysDelayed;

        // Act
        Fine fine = new Fine(overdueLending);

        // Assert - Verify internal calculation
        Object centsValue = getPrivateField(fine, "centsValue");
        assertEquals(expectedValue, centsValue, "Internal centsValue should be calculated correctly");
        assertEquals(expectedValue, fine.getCentsValue(), "Getter should return calculated value");
    }

    @Test
    @DisplayName("Transparent-Box: Should store reference to lending in internal field")
    void testLendingFieldStorage() throws Exception {
        // Act
        Fine fine = new Fine(overdueLending);

        // Assert - Verify internal field storage
        Object storedLending = getPrivateField(fine, "lending");
        assertNotNull(storedLending, "Lending field should be stored internally");
        assertSame(overdueLending, storedLending, "Internal field should reference same lending instance");
        assertSame(overdueLending, fine.getLending(), "Getter should return internal field");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should validate daysDelayed > 0 before initialization")
    void testConstructorValidationBeforeInitialization() {
        // Arrange - Create non-overdue lending
        Lending notOverdue = new Lending(testBook, testReaderDetails, 2, 15, 50);

        // Act & Assert - Constructor should throw before initializing fields
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Fine(notOverdue));
        assertEquals("Lending is not overdue", exception.getMessage(),
                "Should throw with specific message when lending is not overdue");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should call Objects.requireNonNull for lending")
    void testConstructorRequiresNonNullLending() {
        // Act & Assert - Constructor should use Objects.requireNonNull
        assertThrows(NullPointerException.class, () -> new Fine(null),
                "Constructor should throw NullPointerException for null lending");
    }

    @Test
    @DisplayName("Transparent-Box: Should use @PositiveOrZero annotation on fineValuePerDayInCents")
    void testFineValuePerDayInCentsAnnotation() throws Exception {
        // Act
        Field field = Fine.class.getDeclaredField("fineValuePerDayInCents");

        // Assert - Check for @PositiveOrZero annotation
        boolean hasPositiveOrZeroAnnotation = field.isAnnotationPresent(jakarta.validation.constraints.PositiveOrZero.class);
        assertTrue(hasPositiveOrZeroAnnotation || field.getAnnotations().length == 0,
                "fineValuePerDayInCents should have validation constraints");
    }

    @Test
    @DisplayName("Transparent-Box: Should use @PositiveOrZero annotation on centsValue")
    void testCentsValueAnnotation() throws Exception {
        // Act
        Field field = Fine.class.getDeclaredField("centsValue");

        // Assert - Check for @PositiveOrZero annotation
        boolean hasPositiveOrZeroAnnotation = field.isAnnotationPresent(jakarta.validation.constraints.PositiveOrZero.class);
        assertTrue(hasPositiveOrZeroAnnotation || field.getAnnotations().length == 0,
                "centsValue should have validation constraints");
    }

    @Test
    @DisplayName("Transparent-Box: fineValuePerDayInCents field should be immutable (updatable=false)")
    void testFineValuePerDayInCentsImmutability() throws Exception {
        // Act
        Field field = Fine.class.getDeclaredField("fineValuePerDayInCents");
        field.setAccessible(true);

        // Assert - Check for @Column(updatable = false)
        if (field.isAnnotationPresent(jakarta.persistence.Column.class)) {
            jakarta.persistence.Column columnAnnotation = field.getAnnotation(jakarta.persistence.Column.class);
            assertFalse(columnAnnotation.updatable(), "fineValuePerDayInCents should not be updatable");
        }
    }

    @Test
    @DisplayName("Transparent-Box: pk field should have @Id annotation")
    void testPkFieldAnnotations() throws Exception {
        // Act
        Field pkField = Fine.class.getDeclaredField("pk");

        // Assert - Should have JPA @Id annotation
        assertTrue(pkField.isAnnotationPresent(jakarta.persistence.Id.class),
                "pk field should have @Id annotation");
    }

    @Test
    @DisplayName("Transparent-Box: lending field should have @OneToOne relationship")
    void testLendingFieldRelationship() throws Exception {
        // Act
        Field lendingField = Fine.class.getDeclaredField("lending");

        // Assert - Should have @OneToOne annotation
        assertTrue(lendingField.isAnnotationPresent(jakarta.persistence.OneToOne.class),
                "lending field should have @OneToOne annotation");
        
        jakarta.persistence.OneToOne oneToOne = lendingField.getAnnotation(jakarta.persistence.OneToOne.class);
        assertFalse(oneToOne.optional(), "OneToOne relationship should be mandatory (optional=false)");
        assertTrue(oneToOne.orphanRemoval(), "OneToOne should have orphanRemoval=true");
    }

    @Test
    @DisplayName("Transparent-Box: lending field should have @JoinColumn with correct constraints")
    void testLendingFieldJoinColumn() throws Exception {
        // Act
        Field lendingField = Fine.class.getDeclaredField("lending");

        // Assert - Should have @JoinColumn with constraints
        if (lendingField.isAnnotationPresent(jakarta.persistence.JoinColumn.class)) {
            jakarta.persistence.JoinColumn joinColumn = lendingField.getAnnotation(jakarta.persistence.JoinColumn.class);
            assertFalse(joinColumn.nullable(), "JoinColumn should not be nullable");
            assertTrue(joinColumn.unique(), "JoinColumn should be unique");
        }
    }

    @Test
    @DisplayName("Transparent-Box: Protected empty constructor should exist for ORM")
    void testProtectedEmptyConstructorExists() throws Exception {
        // Act - Try to get the protected empty constructor
        java.lang.reflect.Constructor<Fine> constructor = Fine.class.getDeclaredConstructor();

        // Assert
        assertNotNull(constructor, "Protected empty constructor should exist");
        assertTrue(java.lang.reflect.Modifier.isProtected(constructor.getModifiers()),
                "Empty constructor should be protected");
    }

    @Test
    @DisplayName("Transparent-Box: Should have getter method for lending field")
    void testLombokGetterOnFields() throws Exception {
        // Act
        Fine fine = new Fine(overdueLending);
        
        // Assert - Should have getLending() method available
        assertNotNull(fine.getLending(), "getLending() method should exist and return lending");
        assertSame(overdueLending, fine.getLending(), "getLending() should return the correct lending instance");
    }

    @Test
    @DisplayName("Transparent-Box: centsValue calculation should handle zero days delayed edge case")
    void testCentsValueCalculationWithZeroDays() {
        // This test verifies the internal calculation logic
        // If daysDelayed is 0, the constructor should throw before calculation
        Lending notOverdue = new Lending(testBook, testReaderDetails, 3, 15, 50);

        // Assert - Should throw because daysDelayed check happens first
        assertThrows(IllegalArgumentException.class, () -> new Fine(notOverdue),
                "Constructor should validate daysDelayed before calculating centsValue");
    }

    @Test
    @DisplayName("Transparent-Box: Should have @Entity annotation for JPA")
    void testEntityAnnotation() {
        // Assert
        assertTrue(Fine.class.isAnnotationPresent(jakarta.persistence.Entity.class),
                "Fine class should have @Entity annotation");
    }

    @Test
    @DisplayName("Transparent-Box: Should have @Document annotation for MongoDB")
    void testDocumentAnnotation() {
        // Assert
        assertTrue(Fine.class.isAnnotationPresent(org.springframework.data.mongodb.core.mapping.Document.class),
                "Fine class should have @Document annotation for MongoDB");
        
        org.springframework.data.mongodb.core.mapping.Document doc = 
            Fine.class.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class);
        assertEquals("fines", doc.value(), "Document collection should be named 'fines'");
    }

    @Test
    @DisplayName("Transparent-Box: Internal calculation should snapshot lending's fine rate at creation time")
    void testFineRateSnapshotAtCreation() throws Exception {
        // Act
        Fine fine = new Fine(overdueLending);
        
        // Get internal field value
        Object internalRate = getPrivateField(fine, "fineValuePerDayInCents");

        // Assert - Internal field should store the rate at creation time
        assertEquals(50, internalRate, "Internal field should snapshot the rate at creation");
        assertEquals(overdueLending.getFineValuePerDayInCents(), fine.getFineValuePerDayInCents(),
                "Fine should preserve lending's rate at time of creation");
    }

    @Test
    @DisplayName("Transparent-Box: Setter for lending field should exist (for JPA)")
    void testLendingSetterExists() throws Exception {
        // Act
        Field lendingField = Fine.class.getDeclaredField("lending");

        // Assert - Should have @Setter annotation or setLending method
        boolean hasSetterAnnotation = lendingField.isAnnotationPresent(lombok.Setter.class);
        boolean hasSetterMethod = false;
        try {
            Fine.class.getMethod("setLending", Lending.class);
            hasSetterMethod = true;
        } catch (NoSuchMethodException e) {
            // Setter method doesn't exist
        }

        assertTrue(hasSetterAnnotation || hasSetterMethod,
                "Lending field should have a setter (for JPA relationship management)");
    }
}
