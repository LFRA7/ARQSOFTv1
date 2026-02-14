package pt.psoft.g1.psoftg1.readermanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.readermanagement.services.UpdateReaderRequest;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional Transparent-Box Tests for ReaderDetails domain class
 * 
 * Test Strategy: White-box testing with knowledge of internal implementation
 * SUT: ReaderDetails class
 * 
 * Tests internal state, embedded value objects, relationships, and implementation details
 */
@DisplayName("ReaderDetails Transparent-Box Tests")
class ReaderDetailsTransparentBoxTest {

    private Reader validReader;
    private final int validReaderNumber = 1;
    private final String validBirthDate = "2000-01-01";
    private final String validPhoneNumber = "912345678";
    private List<Genre> validInterestList;

    @BeforeEach
    void setUp() {
        validReader = Reader.newReader("reader@test.com", "Password123!", "John Doe");
        validInterestList = new ArrayList<>();
        validInterestList.add(new Genre("Fiction"));
    }

    // Helper method to access private fields using reflection
    private Object getPrivateField(ReaderDetails readerDetails, String fieldName) throws Exception {
        Field field = ReaderDetails.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(readerDetails);
    }

    // Helper method to set version using reflection (simulates JPA behavior)
    private void setVersion(ReaderDetails readerDetails, Long version) throws Exception {
        Field versionField = ReaderDetails.class.getDeclaredField("version");
        versionField.setAccessible(true);
        versionField.set(readerDetails, version);
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should create ReaderNumber value object internally")
    void testConstructorCreatesReaderNumberValueObject() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert - Internal ReaderNumber object should be created
        Object readerNumberObject = getPrivateField(readerDetails, "readerNumber");
        assertNotNull(readerNumberObject, "Internal ReaderNumber object should be created");
        assertTrue(readerNumberObject instanceof ReaderNumber, "Internal field should be ReaderNumber type");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should create BirthDate value object internally")
    void testConstructorCreatesBirthDateValueObject() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert - Internal BirthDate object should be created
        Object birthDateObject = getPrivateField(readerDetails, "birthDate");
        assertNotNull(birthDateObject, "Internal BirthDate object should be created");
        assertTrue(birthDateObject instanceof BirthDate, "Internal field should be BirthDate type");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should create PhoneNumber value object internally")
    void testConstructorCreatesPhoneNumberValueObject() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert - Internal PhoneNumber object should be created
        Object phoneNumberObject = getPrivateField(readerDetails, "phoneNumber");
        assertNotNull(phoneNumberObject, "Internal PhoneNumber object should be created");
        assertTrue(phoneNumberObject instanceof PhoneNumber, "Internal field should be PhoneNumber type");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should set gdprConsent field")
    void testConstructorSetsGdprConsentField() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, false, false, null, validInterestList
        );

        // Assert - Internal field should be set
        Object gdprConsent = getPrivateField(readerDetails, "gdprConsent");
        assertTrue((boolean) gdprConsent, "Internal gdprConsent field should be true");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should set marketingConsent field")
    void testConstructorSetsMarketingConsentField() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, false, null, validInterestList
        );

        // Assert - Internal field should be set
        Object marketingConsent = getPrivateField(readerDetails, "marketingConsent");
        assertTrue((boolean) marketingConsent, "Internal marketingConsent field should be true");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should set thirdPartySharingConsent field")
    void testConstructorSetsThirdPartySharingConsentField() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, false, true, null, validInterestList
        );

        // Assert - Internal field should be set
        Object thirdPartySharingConsent = getPrivateField(readerDetails, "thirdPartySharingConsent");
        assertTrue((boolean) thirdPartySharingConsent, "Internal thirdPartySharingConsent field should be true");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should store reference to Reader")
    void testConstructorStoresReaderReference() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert - Internal field should reference the reader
        Object storedReader = getPrivateField(readerDetails, "reader");
        assertSame(validReader, storedReader, "Internal field should reference same Reader instance");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should store interestList reference")
    void testConstructorStoresInterestListReference() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert - Internal field should reference the interest list
        Object storedList = getPrivateField(readerDetails, "interestList");
        assertNotNull(storedList, "Internal interestList should be set");
    }

    @Test
    @DisplayName("Transparent-Box: pk field should be null before persistence")
    void testPkFieldInitializedAsNull() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert
        Object pk = getPrivateField(readerDetails, "pk");
        assertNull(pk, "PK should be null before persistence");
    }

    @Test
    @DisplayName("Transparent-Box: version field should be null initially")
    void testVersionFieldInitializedAsNull() throws Exception {
        // Act
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert
        assertNull(readerDetails.getVersion(), "Version should be null before persistence");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should replace BirthDate value object when provided")
    void testApplyPatchReplacesBirthDateValueObject() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L);
        Object originalBirthDate = getPrivateField(readerDetails, "birthDate");

        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setBirthDate("1995-05-15");

        // Act
        readerDetails.applyPatch(1L, request, null, null);

        // Assert - BirthDate object should be replaced
        Object newBirthDate = getPrivateField(readerDetails, "birthDate");
        assertNotSame(originalBirthDate, newBirthDate, "BirthDate object should be replaced");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should replace PhoneNumber value object when provided")
    void testApplyPatchReplacesPhoneNumberValueObject() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L);
        Object originalPhoneNumber = getPrivateField(readerDetails, "phoneNumber");

        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setPhoneNumber("919999999");

        // Act
        readerDetails.applyPatch(1L, request, null, null);

        // Assert - PhoneNumber object should be replaced
        Object newPhoneNumber = getPrivateField(readerDetails, "phoneNumber");
        assertNotSame(originalPhoneNumber, newPhoneNumber, "PhoneNumber object should be replaced");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should update marketingConsent field directly")
    void testApplyPatchUpdatesMarketingConsentField() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L);

        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setMarketing(false);

        // Act
        readerDetails.applyPatch(1L, request, null, null);

        // Assert - Internal field should be updated
        Object marketingConsent = getPrivateField(readerDetails, "marketingConsent");
        assertFalse((boolean) marketingConsent, "Internal marketingConsent field should be false");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should update thirdPartySharingConsent field directly")
    void testApplyPatchUpdatesThirdPartySharingConsentField() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L);

        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setThirdParty(false);

        // Act
        readerDetails.applyPatch(1L, request, null, null);

        // Assert - Internal field should be updated
        Object thirdParty = getPrivateField(readerDetails, "thirdPartySharingConsent");
        assertFalse((boolean) thirdParty, "Internal thirdPartySharingConsent field should be false");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should update Reader name through reader reference")
    void testApplyPatchUpdatesReaderName() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L);
        String originalName = validReader.getName().toString();

        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setFullName("Jane Smith");

        // Act
        readerDetails.applyPatch(1L, request, null, null);

        // Assert - Reader's name should be updated
        assertNotEquals(originalName, readerDetails.getReader().getName().toString(),
                "Reader's name should be updated");
        assertEquals("Jane Smith", readerDetails.getReader().getName().toString(),
                "Reader's name should match request");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should replace interestList when provided")
    void testApplyPatchReplacesInterestList() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L);
        Object originalList = getPrivateField(readerDetails, "interestList");

        List<Genre> newInterestList = new ArrayList<>();
        newInterestList.add(new Genre("Science"));

        UpdateReaderRequest request = new UpdateReaderRequest();

        // Act
        readerDetails.applyPatch(1L, request, null, newInterestList);

        // Assert - InterestList should be replaced
        Object newList = getPrivateField(readerDetails, "interestList");
        assertNotSame(originalList, newList, "InterestList should be replaced");
    }

    @Test
    @DisplayName("Transparent-Box: applyPatch should throw ConflictException for version mismatch")
    void testApplyPatchThrowsConflictExceptionForVersionMismatch() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 5L);

        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setPhoneNumber("919999999");

        // Act & Assert
        assertThrows(ConflictException.class, () -> 
            readerDetails.applyPatch(3L, request, null, null),
            "Should throw ConflictException when version mismatch");
    }

    @Test
    @DisplayName("Transparent-Box: removePhoto should throw ConflictException for version mismatch")
    void testRemovePhotoThrowsConflictExceptionForVersionMismatch() throws Exception {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, "photo.jpg", validInterestList
        );
        setVersion(readerDetails, 5L);

        // Act & Assert
        assertThrows(ConflictException.class, () -> 
            readerDetails.removePhoto(3L),
            "Should throw ConflictException when version mismatch");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should throw IllegalArgumentException for null reader")
    void testConstructorThrowsForNullReader() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            new ReaderDetails(validReaderNumber, null, validBirthDate, validPhoneNumber,
                true, true, true, null, validInterestList),
            "Constructor should throw IllegalArgumentException for null reader");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should throw IllegalArgumentException for null phoneNumber")
    void testConstructorThrowsForNullPhoneNumber() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            new ReaderDetails(validReaderNumber, validReader, validBirthDate, null,
                true, true, true, null, validInterestList),
            "Constructor should throw IllegalArgumentException for null phoneNumber");
    }

    @Test
    @DisplayName("Transparent-Box: Constructor should throw IllegalArgumentException for false GDPR consent")
    void testConstructorThrowsForFalseGdprConsent() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            new ReaderDetails(validReaderNumber, validReader, validBirthDate, validPhoneNumber,
                false, true, true, null, validInterestList),
            "Constructor should throw IllegalArgumentException when GDPR consent is false");
    }

    @Test
    @DisplayName("Transparent-Box: BirthDate field should have @Embedded annotation")
    void testBirthDateFieldHasEmbeddedAnnotation() throws Exception {
        // Act
        Field birthDateField = ReaderDetails.class.getDeclaredField("birthDate");

        // Assert
        assertTrue(birthDateField.isAnnotationPresent(jakarta.persistence.Embedded.class),
                "BirthDate field should have @Embedded annotation");
    }

    @Test
    @DisplayName("Transparent-Box: PhoneNumber field should have @Embedded annotation")
    void testPhoneNumberFieldHasEmbeddedAnnotation() throws Exception {
        // Act
        Field phoneNumberField = ReaderDetails.class.getDeclaredField("phoneNumber");

        // Assert
        assertTrue(phoneNumberField.isAnnotationPresent(jakarta.persistence.Embedded.class),
                "PhoneNumber field should have @Embedded annotation");
    }

    @Test
    @DisplayName("Transparent-Box: reader field should have @OneToOne relationship")
    void testReaderFieldHasOneToOneRelationship() throws Exception {
        // Act
        Field readerField = ReaderDetails.class.getDeclaredField("reader");

        // Assert
        assertTrue(readerField.isAnnotationPresent(jakarta.persistence.OneToOne.class),
                "reader field should have @OneToOne annotation");
    }

    @Test
    @DisplayName("Transparent-Box: interestList field should have @ManyToMany relationship")
    void testInterestListFieldHasManyToManyRelationship() throws Exception {
        // Act
        Field interestListField = ReaderDetails.class.getDeclaredField("interestList");

        // Assert
        assertTrue(interestListField.isAnnotationPresent(jakarta.persistence.ManyToMany.class),
                "interestList field should have @ManyToMany annotation");
    }

    @Test
    @DisplayName("Transparent-Box: version field should have @Version annotation")
    void testVersionFieldHasVersionAnnotation() throws Exception {
        // Act
        Field versionField = ReaderDetails.class.getDeclaredField("version");

        // Assert
        assertTrue(versionField.isAnnotationPresent(jakarta.persistence.Version.class),
                "version field should have @Version annotation for optimistic locking");
    }

    @Test
    @DisplayName("Transparent-Box: ReaderDetails class should extend EntityWithPhoto")
    void testReaderDetailsExtendsEntityWithPhoto() {
        // Arrange
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );

        // Assert
        assertTrue(readerDetails instanceof pt.psoft.g1.psoftg1.shared.model.EntityWithPhoto,
                "ReaderDetails should extend EntityWithPhoto");
    }

    @Test
    @DisplayName("Transparent-Box: ReaderDetails class should have @Entity annotation")
    void testReaderDetailsHasEntityAnnotation() {
        // Assert
        assertTrue(ReaderDetails.class.isAnnotationPresent(jakarta.persistence.Entity.class),
                "ReaderDetails class should have @Entity annotation");
    }

    @Test
    @DisplayName("Transparent-Box: ReaderDetails class should have @Document annotation for MongoDB")
    void testReaderDetailsHasDocumentAnnotation() {
        // Assert
        assertTrue(ReaderDetails.class.isAnnotationPresent(
                org.springframework.data.mongodb.core.mapping.Document.class),
                "ReaderDetails class should have @Document annotation for MongoDB");
        
        org.springframework.data.mongodb.core.mapping.Document doc = 
            ReaderDetails.class.getAnnotation(org.springframework.data.mongodb.core.mapping.Document.class);
        assertEquals("readerdetails", doc.value(), "Document collection should be named 'readerdetails'");
    }
}
