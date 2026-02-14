package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test - Functional transparent-box testing with SUT = Book domain class
 * Tests internal state and behavior of the Book class with knowledge of implementation
 */
class BookTransparentBoxTest {

    private Genre validGenre;
    private List<Author> validAuthors;
    private final String validIsbn = "9783161484100"; // Valid ISBN-13
    private final String validTitle = "Test Book Title";
    private final String validDescription = "Test description";

    @BeforeEach
    void setUp() {
        validGenre = new Genre("Fiction");
        Author author = new Author("Test Author", "Author biography", null);
        validAuthors = new ArrayList<>();
        validAuthors.add(author);
    }
    
    // Helper method to set version using reflection (simulates JPA behavior)
    private void setVersion(Book book, Long version) {
        try {
            Field versionField = Book.class.getDeclaredField("version");
            versionField.setAccessible(true);
            versionField.set(book, version);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set version", e);
        }
    }

    // Functional transparent-box test: Testing internal setTitle method through constructor
    @Test
    void testInternalSetTitleThroughConstructor() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify that the internal Title object is properly created
        assertNotNull(book.getTitle());
        assertEquals(validTitle, book.getTitle().toString());
    }

    // Functional transparent-box test: Testing internal setIsbn method through constructor
    @Test
    void testInternalSetIsbnThroughConstructor() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify that the internal Isbn object is properly created
        assertNotNull(book.getIsbn());
        assertEquals(validIsbn, book.getIsbn());
    }

    // Functional transparent-box test: Testing internal setDescription method
    @Test
    void testInternalSetDescriptionThroughConstructor() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify that the internal Description object is properly created
        assertNotNull(book.getDescription());
        assertEquals(validDescription, book.getDescription());
    }

    // Functional transparent-box test: Testing internal setGenre method
    @Test
    void testInternalSetGenreThroughConstructor() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify that the genre reference is correctly stored
        assertSame(validGenre, book.getGenre());
    }

    // Functional transparent-box test: Testing internal setAuthors method
    @Test
    void testInternalSetAuthorsThroughConstructor() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify that the authors list reference is correctly stored
        assertNotNull(book.getAuthors());
        assertEquals(1, book.getAuthors().size());
        assertSame(validAuthors.get(0), book.getAuthors().get(0));
    }

    // Functional transparent-box test: Testing internal setPhotoInternal method with null
    @Test
    void testInternalSetPhotoInternalWithNull() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify that photo is null when not provided
        assertNull(book.getPhoto());
    }

    // Functional transparent-box test: Testing internal setPhotoInternal method with URI
    @Test
    void testInternalSetPhotoInternalWithUri() {
        String photoUri = "test.jpg";
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, photoUri);
        
        // Verify that photo object is created when URI is provided
        assertNotNull(book.getPhoto());
        assertEquals(photoUri, book.getPhoto().getPhotoFile());
    }

    // Functional transparent-box test: Testing version field initialization
    @Test
    void testVersionFieldInitialization() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Version should be null before persistence
        assertNull(book.getVersion());
    }

    // Functional transparent-box test: Testing applyPatch internal field updates
    @Test
    void testApplyPatchInternalFieldUpdates() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        Long currentVersion = book.getVersion();
        
        // Create request with all fields
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("New Title");
        request.setDescription("New Description");
        Genre newGenre = new Genre("Science");
        request.setGenreObj(newGenre);
        
        book.applyPatch(currentVersion, request);
        
        // Verify all internal fields are updated
        assertEquals("New Title", book.getTitle().toString());
        assertEquals("New Description", book.getDescription());
        assertSame(newGenre, book.getGenre());
    }

    // Functional transparent-box test: Testing applyPatch selective updates
    @Test
    void testApplyPatchSelectiveUpdates() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        Long currentVersion = book.getVersion();
        
        String originalDescription = book.getDescription();
        Genre originalGenre = book.getGenre();
        
        // Update only title
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated Title Only");
        
        book.applyPatch(currentVersion, request);
        
        // Verify only title changed, others remain the same
        assertEquals("Updated Title Only", book.getTitle().toString());
        assertEquals(originalDescription, book.getDescription());
        assertSame(originalGenre, book.getGenre());
    }

    // Functional transparent-box test: Testing removePhoto internal state change
    @Test
    void testRemovePhotoInternalStateChange() {
        String photoUri = "test.jpg";
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, photoUri);
        setVersion(book, 1L); // Simulate persisted entity
        
        assertNotNull(book.getPhoto());
        Long currentVersion = book.getVersion();
        
        book.removePhoto(currentVersion);
        
        // Verify photo is set to null internally
        assertNull(book.getPhoto());
    }

    // Functional transparent-box test: Testing EntityWithPhoto inheritance
    @Test
    void testEntityWithPhotoInheritance() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify Book extends EntityWithPhoto and has photo functionality
        assertDoesNotThrow(() -> book.setPhoto("newPhoto.jpg"));
        assertNotNull(book.getPhoto());
    }

    // Functional transparent-box test: Testing embedded objects creation
    @Test
    void testEmbeddedObjectsCreation() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        // Verify that embedded value objects (Title, Isbn, Description) are created
        assertNotNull(book.getTitle());
        assertNotNull(book.getIsbn());
        assertNotNull(book.getDescription());
        
        // These should be value objects, not null references
        assertTrue(book.getTitle() instanceof Title);
        assertEquals(validIsbn, book.getIsbn());
    }

    // Functional transparent-box test: Testing ManyToMany relationship with authors
    @Test
    void testManyToManyAuthorsRelationship() {
        Author author1 = new Author("Author 1", "Bio 1", null);
        Author author2 = new Author("Author 2", "Bio 2", null);
        List<Author> multipleAuthors = new ArrayList<>();
        multipleAuthors.add(author1);
        multipleAuthors.add(author2);
        
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, multipleAuthors, null);
        
        // Verify that the collection properly stores multiple authors
        assertEquals(2, book.getAuthors().size());
        assertTrue(book.getAuthors().contains(author1));
        assertTrue(book.getAuthors().contains(author2));
    }

    // Functional transparent-box test: Testing ManyToOne relationship with genre
    @Test
    void testManyToOneGenreRelationship() {
        Genre sharedGenre = new Genre("Shared Genre");
        
        Book book1 = new Book(validIsbn, "Book 1", validDescription, sharedGenre, validAuthors, null);
        Book book2 = new Book("9780306406157", "Book 2", validDescription, sharedGenre, validAuthors, null); // Valid ISBN-13
        
        // Verify both books reference the same genre instance
        assertSame(sharedGenre, book1.getGenre());
        assertSame(sharedGenre, book2.getGenre());
        assertSame(book1.getGenre(), book2.getGenre());
    }
}
