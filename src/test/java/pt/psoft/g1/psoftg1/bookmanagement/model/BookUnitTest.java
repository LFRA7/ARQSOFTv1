package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.bookmanagement.services.UpdateBookRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test - Functional opaque-box testing with SUT = Book class
 * Tests the Book domain class in isolation
 */
class BookUnitTest {

    private Genre validGenre;
    private Author validAuthor;
    private List<Author> validAuthors;
    private String validIsbn;
    private String validTitle;
    private String validDescription;

    @BeforeEach
    void setUp() {
        validGenre = new Genre("Fiction");
        validAuthor = new Author("Test Author", "Test Bio", null);
        validAuthors = new ArrayList<>();
        validAuthors.add(validAuthor);
        validIsbn = "9783161484100";
        validTitle = "Test Book";
        validDescription = "Test Description";
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

    // Functional opaque-box test: Valid book creation
    @Test
    void testCreateValidBookWithAllFields() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        assertNotNull(book);
        assertEquals(validIsbn, book.getIsbn());
        assertEquals(validTitle, book.getTitle().toString());
        assertEquals(validDescription, book.getDescription());
        assertEquals(validGenre, book.getGenre());
        assertEquals(1, book.getAuthors().size());
        assertNull(book.getPhoto());
    }

    // Functional opaque-box test: Book creation with photo
    @Test
    void testCreateBookWithPhoto() {
        String photoUri = "bookPhoto.jpg";
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, photoUri);
        
        assertNotNull(book.getPhoto());
        assertEquals(photoUri, book.getPhoto().getPhotoFile());
    }

    // Functional opaque-box test: Null ISBN validation
    @Test
    void testCreateBookWithNullIsbnThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Book(null, validTitle, validDescription, validGenre, validAuthors, null)
        );
    }

    // Functional opaque-box test: Null title validation
    @Test
    void testCreateBookWithNullTitleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Book(validIsbn, null, validDescription, validGenre, validAuthors, null)
        );
    }

    // Functional opaque-box test: Null genre validation
    @Test
    void testCreateBookWithNullGenreThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Book(validIsbn, validTitle, validDescription, null, validAuthors, null)
        );
    }

    // Functional opaque-box test: Null authors list validation
    @Test
    void testCreateBookWithNullAuthorsListThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Book(validIsbn, validTitle, validDescription, validGenre, null, null)
        );
    }

    // Functional opaque-box test: Empty authors list validation
    @Test
    void testCreateBookWithEmptyAuthorsListThrowsException() {
        List<Author> emptyAuthors = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> 
            new Book(validIsbn, validTitle, validDescription, validGenre, emptyAuthors, null)
        );
    }

    // Functional opaque-box test: Multiple authors
    @Test
    void testCreateBookWithMultipleAuthors() {
        Author author2 = new Author("Second Author", "Second author bio", null);
        validAuthors.add(author2);
        
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        assertEquals(2, book.getAuthors().size());
    }

    // Functional opaque-box test: Book without description
    @Test
    void testCreateBookWithoutDescription() {
        Book book = new Book(validIsbn, validTitle, null, validGenre, validAuthors, null);
        
        assertNotNull(book);
        // Description is null internally, getDescription() throws NPE
        assertThrows(NullPointerException.class, () -> book.getDescription());
    }

    // Functional opaque-box test: Apply patch to update title
    @Test
    void testApplyPatchUpdatesTitle() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        Long currentVersion = book.getVersion();
        
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated Title");
        
        book.applyPatch(currentVersion, request);
        
        assertEquals("Updated Title", book.getTitle().toString());
    }

    // Functional opaque-box test: Apply patch to update description
    @Test
    void testApplyPatchUpdatesDescription() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        Long currentVersion = book.getVersion();
        
        UpdateBookRequest request = new UpdateBookRequest();
        request.setDescription("Updated description for the book.");
        
        book.applyPatch(currentVersion, request);
        
        assertEquals("Updated description for the book.", book.getDescription());
    }

    // Functional opaque-box test: Apply patch to update genre
    @Test
    void testApplyPatchUpdatesGenre() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        Long currentVersion = book.getVersion();
        
        Genre newGenre = new Genre("Science");
        UpdateBookRequest request = new UpdateBookRequest();
        request.setGenreObj(newGenre);
        
        book.applyPatch(currentVersion, request);
        
        assertEquals(newGenre, book.getGenre());
    }

    // Functional opaque-box test: Apply patch to update authors
    @Test
    void testApplyPatchUpdatesAuthors() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        Long currentVersion = book.getVersion();
        
        List<Author> newAuthors = new ArrayList<>();
        newAuthors.add(new Author("New Author", "New bio", null));
        
        UpdateBookRequest request = new UpdateBookRequest();
        request.setAuthorObjList(newAuthors);
        
        book.applyPatch(currentVersion, request);
        
        assertEquals(1, book.getAuthors().size());
        assertEquals("New Author", book.getAuthors().get(0).getName().toString());
    }

    // Functional opaque-box test: Apply patch with stale version throws exception
    @Test
    void testApplyPatchWithStaleVersionThrowsException() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        
        UpdateBookRequest request = new UpdateBookRequest();
        request.setTitle("Updated Title");
        
        assertThrows(StaleObjectStateException.class, () -> 
            book.applyPatch(999L, request)
        );
    }

    // Functional opaque-box test: Remove photo
    @Test
    void testRemovePhoto() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, "photo.jpg");
        setVersion(book, 1L); // Simulate persisted entity
        assertNotNull(book.getPhoto());
        
        Long currentVersion = book.getVersion();
        book.removePhoto(currentVersion);
        
        assertNull(book.getPhoto());
    }

    // Functional opaque-box test: Remove photo with wrong version throws exception
    @Test
    void testRemovePhotoWithWrongVersionThrowsException() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, "photo.jpg");
        setVersion(book, 1L); // Simulate persisted entity
        
        assertThrows(ConflictException.class, () -> 
            book.removePhoto(999L)
        );
    }

    // Functional opaque-box test: Apply patch with null fields doesn't change values
    @Test
    void testApplyPatchWithNullFieldsDoesNotChangeValues() {
        Book book = new Book(validIsbn, validTitle, validDescription, validGenre, validAuthors, null);
        setVersion(book, 1L); // Simulate persisted entity
        Long currentVersion = book.getVersion();
        String originalTitle = book.getTitle().toString();
        String originalDescription = book.getDescription();
        
        UpdateBookRequest request = new UpdateBookRequest();
        // All fields are null
        
        book.applyPatch(currentVersion, request);
        
        assertEquals(originalTitle, book.getTitle().toString());
        assertEquals(originalDescription, book.getDescription());
    }
}
