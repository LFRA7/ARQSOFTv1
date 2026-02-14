package pt.psoft.g1.psoftg1.authormanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.authormanagement.services.UpdateAuthorRequest;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.shared.model.Name;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional Transparent-Box Tests for Author domain class
 * 
 * Test Strategy: White-box testing with knowledge of internal implementation
 * SUT: Author class
 */
@DisplayName("Author Transparent-Box Tests")
class AuthorTransparentBoxTest {

    private final String validName = "John Doe";
    private final String validBio = "A renowned author with multiple bestsellers";
    private final String validPhotoUri = "author-photo.jpg";

    @BeforeEach
    void setUp() {

    }

    // Helper method to access private fields using reflection
    private Object getPrivateField(Author author, String fieldName) throws Exception {
        Field field = Author.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(author);
    }

    // Helper method to set version using reflection (simulates JPA behavior)
    private void setVersion(Author author, Long version) throws Exception {
        Field versionField = Author.class.getDeclaredField("version");
        versionField.setAccessible(true);
        versionField.set(author, version);
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should create Name value object internally")
    void testConstructorCreatesNameValueObject() throws Exception {
        // Act
        Author author = new Author(validName, validBio, null);

        // Assert - Internal Name object should be created
        Object nameObject = getPrivateField(author, "name");
        assertNotNull(nameObject, "Internal Name object should be created");
        assertTrue(nameObject instanceof Name, "Internal field should be Name type");
        assertEquals(validName, author.getName(), "getName() should return Name.toString()");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should create Bio value object internally")
    void testConstructorCreatesBioValueObject() throws Exception {
        // Act
        Author author = new Author(validName, validBio, null);

        // Assert - Internal Bio object should be created
        Object bioObject = getPrivateField(author, "bio");
        assertNotNull(bioObject, "Internal Bio object should be created");
        assertTrue(bioObject instanceof Bio, "Internal field should be Bio type");
        assertEquals(validBio, author.getBio(), "getBio() should return Bio.toString()");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should call setPhotoInternal for photo")
    void testConstructorCallsSetPhotoInternal() throws Exception {
        // Act
        Author author = new Author(validName, validBio, validPhotoUri);

        // Assert - Photo should be set through inherited setPhotoInternal method
        assertNotNull(author.getPhoto(), "Photo should be set");
        assertEquals(validPhotoUri, author.getPhoto().getPhotoFile(), "Photo URI should match");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor with null photo should not set photo")
    void testConstructorWithNullPhoto() {
        // Act
        Author author = new Author(validName, validBio, null);

        // Assert - Photo should be null
        assertNull(author.getPhoto(), "Photo should be null when not provided");
    }

    @Test
    @DisplayName("Transparent-Box: setName should replace internal Name value object")
    void testSetNameReplacesValueObject() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        Object originalNameObject = getPrivateField(author, "name");

        // Act
        author.setName("Jane Smith");

        // Assert - New Name object should be created
        Object newNameObject = getPrivateField(author, "name");
        assertNotNull(newNameObject, "New Name object should exist");
        assertNotSame(originalNameObject, newNameObject, "Name object should be replaced, not modified");
        assertEquals("Jane Smith", author.getName(), "Name should be updated");
    }

    @Test
    @DisplayName("Transparent-Box: setBio should replace internal Bio value object")
    void testSetBioReplacesValueObject() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        Object originalBioObject = getPrivateField(author, "bio");

        // Act
        author.setBio("Updated biography");

        // Assert - New Bio object should be created
        Object newBioObject = getPrivateField(author, "bio");
        assertNotNull(newBioObject, "New Bio object should exist");
        assertNotSame(originalBioObject, newBioObject, "Bio object should be replaced, not modified");
        assertEquals("Updated biography", author.getBio(), "Bio should be updated");
    }

    @Test
    @DisplayName("Transparent-Box: authorNumber field should be initialized as null before persistence")
    void testAuthorNumberInitializedAsNull() throws Exception {
        // Act
        Author author = new Author(validName, validBio, null);

        // Assert - authorNumber should be null before persistence
        assertNull(author.getAuthorNumber(), "authorNumber should be null before persistence");
    }

    @Test
    @DisplayName("Transparent-Box: version field should be 0 initially")
    void testVersionFieldInitializedAsZero() throws Exception {
        // Act
        Author author = new Author(validName, validBio, null);

        // Assert - version should be 0 initially
        Object version = getPrivateField(author, "version");
        assertEquals(0L, version, "Version should be 0 before any updates");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should update internal Name object when name provided")
    void testApplyPatchUpdatesNameObject() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        setVersion(author, 1L);
        Object originalNameObject = getPrivateField(author, "name");

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName("Updated Name");

        // Act
        author.applyPatch(1L, request);

        // Assert - Name object should be replaced
        Object newNameObject = getPrivateField(author, "name");
        assertNotSame(originalNameObject, newNameObject, "Name object should be replaced");
        assertEquals("Updated Name", author.getName(), "Name should be updated");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should update internal Bio object when bio provided")
    void testApplyPatchUpdatesBioObject() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        setVersion(author, 1L);
        Object originalBioObject = getPrivateField(author, "bio");

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setBio("Updated Bio");

        // Act
        author.applyPatch(1L, request);

        // Assert - Bio object should be replaced
        Object newBioObject = getPrivateField(author, "bio");
        assertNotSame(originalBioObject, newBioObject, "Bio object should be replaced");
        assertEquals("Updated Bio", author.getBio(), "Bio should be updated");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should not modify fields when request values are null")
    void testApplyPatchWithNullRequestValues() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, validPhotoUri);
        setVersion(author, 1L);
        Object originalNameObject = getPrivateField(author, "name");
        Object originalBioObject = getPrivateField(author, "bio");

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        // All fields null

        // Act
        author.applyPatch(1L, request);

        // Assert - Internal objects should remain unchanged
        Object currentNameObject = getPrivateField(author, "name");
        Object currentBioObject = getPrivateField(author, "bio");
        assertSame(originalNameObject, currentNameObject, "Name object should not change");
        assertSame(originalBioObject, currentBioObject, "Bio object should not change");
        assertEquals(validName, author.getName(), "Name should remain unchanged");
        assertEquals(validBio, author.getBio(), "Bio should remain unchanged");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should throw StaleObjectStateException for wrong version")
    void testApplyPatchThrowsStaleObjectExceptionForWrongVersion() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        setVersion(author, 5L);

        UpdateAuthorRequest request = new UpdateAuthorRequest();
        request.setName("New Name");

        // Act & Assert - Should throw StaleObjectStateException
        assertThrows(org.hibernate.StaleObjectStateException.class, () -> 
            author.applyPatch(3L, request),
            "Should throw StaleObjectStateException when version mismatch");
    }

    @Test
    @DisplayName("Transparent-Box: removePhoto should call setPhotoInternal with null")
    void testRemovePhotoCallsSetPhotoInternalWithNull() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, validPhotoUri);
        setVersion(author, 1L);
        assertNotNull(author.getPhoto(), "Photo should be set initially");

        // Act
        author.removePhoto(1L);

        // Assert - Photo should be null after removal
        assertNull(author.getPhoto(), "Photo should be null after removal");
    }

    @Test
    @DisplayName("Transparent-Box: removePhoto should throw ConflictException for wrong version")
    void testRemovePhotoThrowsConflictExceptionForWrongVersion() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, validPhotoUri);
        setVersion(author, 5L);

        // Act & Assert
        assertThrows(ConflictException.class, () -> author.removePhoto(3L),
                "Should throw ConflictException when version mismatch");
    }

    @Test
    @DisplayName("Transparent-Box: getId should return authorNumber field value")
    void testGetIdReturnsAuthorNumber() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        try {
            setVersion(author, 1L);
        } catch (Exception e) {
            fail("Failed to set version: " + e.getMessage());
        }
        
        // Simulate authorNumber being set by persistence layer
        Field authorNumberField = Author.class.getDeclaredField("authorNumber");
        authorNumberField.setAccessible(true);
        authorNumberField.set(author, 123L);

        // Act & Assert
        assertEquals(123L, author.getId(), "getId() should return authorNumber field value");
        assertEquals(123L, author.getAuthorNumber(), "getAuthorNumber() should return same value");
        assertSame(author.getId(), author.getAuthorNumber(), "Both methods should return same reference");
    }

    @Test
    @DisplayName("Transparent-Box: getName should return Name.toString()")
    void testGetNameReturnsNameToString() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        Object nameObject = getPrivateField(author, "name");

        // Act & Assert
        assertEquals(nameObject.toString(), author.getName(),
                "getName() should return Name.toString()");
    }

    @Test
    @DisplayName("Transparent-Box: getBio should return Bio.toString()")
    void testGetBioReturnsBioToString() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        Object bioObject = getPrivateField(author, "bio");

        // Act & Assert
        assertEquals(bioObject.toString(), author.getBio(),
                "getBio() should return Bio.toString()");
    }

    @Test
    @DisplayName("Transparent-Box: Protected empty constructor should exist for ORM")
    void testProtectedEmptyConstructorExists() throws Exception {
        // Act - Get protected constructor
        java.lang.reflect.Constructor<Author> constructor = Author.class.getDeclaredConstructor();

        // Assert
        assertNotNull(constructor, "Protected empty constructor should exist");
        assertTrue(java.lang.reflect.Modifier.isProtected(constructor.getModifiers()),
                "Empty constructor should be protected");
    }

    @Test
    @DisplayName("Transparent-Box: Name field should have @Embedded annotation")
    void testNameFieldHasEmbeddedAnnotation() throws Exception {
        // Act
        Field nameField = Author.class.getDeclaredField("name");

        // Assert
        assertTrue(nameField.isAnnotationPresent(jakarta.persistence.Embedded.class),
                "Name field should have @Embedded annotation");
    }

    @Test
    @DisplayName("Transparent-Box: Bio field should have @Embedded annotation")
    void testBioFieldHasEmbeddedAnnotation() throws Exception {
        // Act
        Field bioField = Author.class.getDeclaredField("bio");

        // Assert
        assertTrue(bioField.isAnnotationPresent(jakarta.persistence.Embedded.class),
                "Bio field should have @Embedded annotation");
    }

    @Test
    @DisplayName("Transparent-Box: authorNumber field should have @Id annotation")
    void testAuthorNumberFieldHasIdAnnotation() throws Exception {
        // Act
        Field authorNumberField = Author.class.getDeclaredField("authorNumber");

        // Assert
        assertTrue(authorNumberField.isAnnotationPresent(jakarta.persistence.Id.class),
                "authorNumber field should have @Id annotation");
    }

    @Test
    @DisplayName("Transparent-Box: version field should have @Version annotation")
    void testVersionFieldHasVersionAnnotation() throws Exception {
        // Act
        Field versionField = Author.class.getDeclaredField("version");

        // Assert
        assertTrue(versionField.isAnnotationPresent(jakarta.persistence.Version.class),
                "version field should have @Version annotation for optimistic locking");
    }

    @Test
    @DisplayName("Transparent-Box: Author class should extend EntityWithPhoto")
    void testAuthorExtendsEntityWithPhoto() {
        // Arrange
        Author author = new Author(validName, validBio, null);

        // Assert
        assertTrue(author instanceof pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto,
                "Author should extend EntityWithPhoto");
    }

    @Test
    @DisplayName("Transparent-Box: Author class should have @Entity annotation")
    void testAuthorHasEntityAnnotation() {
        // Assert
        assertTrue(Author.class.isAnnotationPresent(jakarta.persistence.Entity.class),
                "Author class should have @Entity annotation");
    }

    @Test
    @DisplayName("Transparent-Box: Author class should have @Document annotation for MongoDB")
    void testAuthorHasDocumentAnnotation() {
        // Assert
        assertTrue(Author.class.isAnnotationPresent(
                org.springframework.data.mongodb.core.mapping.Document.class),
                "Author class should have @Document annotation for MongoDB");
        
        org.springframework.data.mongodb.core.mapping.Document doc = 
            Author.class.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class);
        assertEquals("authors", doc.value(), "Document collection should be named 'authors'");
    }

    @Test
    @DisplayName("Transparent-Box: Author class should implement Serializable")
    void testAuthorImplementsSerializable() {
        // Arrange
        Author author = new Author(validName, validBio, null);

        // Assert
        assertTrue(author instanceof java.io.Serializable,
                "Author should implement Serializable");
    }

    @Test
    @DisplayName("Transparent-Box: Value objects should be immutable from Author's perspective")
    void testValueObjectsImmutability() throws Exception {
        // Arrange
        Author author = new Author(validName, validBio, null);
        Object nameObject1 = getPrivateField(author, "name");
        Object bioObject1 = getPrivateField(author, "bio");

        // Act - Access the getters multiple times
        author.getName();
        author.getBio();
        
        Object nameObject2 = getPrivateField(author, "name");
        Object bioObject2 = getPrivateField(author, "bio");

        // Assert - Same object references should be maintained
        assertSame(nameObject1, nameObject2, "Name object should remain the same");
        assertSame(bioObject1, bioObject2, "Bio object should remain the same");
    }
}
