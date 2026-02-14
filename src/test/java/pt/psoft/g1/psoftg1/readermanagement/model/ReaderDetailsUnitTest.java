package pt.psoft.g1.psoftg1.readermanagement.model;

import org.junit.jupiter.api.BeforeEach;
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
 * Unit Test - Functional opaque-box testing with SUT = ReaderDetails class
 * Tests the ReaderDetails domain class in isolation
 */
class ReaderDetailsUnitTest {

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
    
    // Helper method to set version using reflection (simulates JPA behavior)
    private void setVersion(ReaderDetails readerDetails, Long version) {
        try {
            Field versionField = ReaderDetails.class.getDeclaredField("version");
            versionField.setAccessible(true);
            versionField.set(readerDetails, version);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set version", e);
        }
    }

    // Functional opaque-box test: Valid reader details creation
    @Test
    void testCreateValidReaderDetails() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        
        assertNotNull(readerDetails);
        assertEquals(validReader, readerDetails.getReader());
        // Compare as LocalDate format (actual format from BirthDate.toString())
        assertEquals("2000-1-1", readerDetails.getBirthDate().toString());
        assertEquals(validPhoneNumber, readerDetails.getPhoneNumber());
        assertTrue(readerDetails.isGdprConsent());
        assertTrue(readerDetails.isMarketingConsent());
        assertTrue(readerDetails.isThirdPartySharingConsent());
        assertNull(readerDetails.getPhoto());
    }

    // Functional opaque-box test: Reader details with photo
    @Test
    void testCreateReaderDetailsWithPhoto() {
        String photoUri = "readerPhoto.jpg";
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, false, false, photoUri, validInterestList
        );
        
        assertNotNull(readerDetails.getPhoto());
        assertEquals(photoUri, readerDetails.getPhoto().getPhotoFile());
    }

    // Functional opaque-box test: Reader details without marketing consent
    @Test
    void testCreateReaderDetailsWithoutMarketingConsent() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, false, true, null, validInterestList
        );
        
        assertFalse(readerDetails.isMarketingConsent());
        assertTrue(readerDetails.isThirdPartySharingConsent());
    }

    // Functional opaque-box test: Null reader throws exception
    @Test
    void testCreateReaderDetailsWithNullReaderThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ReaderDetails(validReaderNumber, null, validBirthDate, validPhoneNumber,
                true, true, true, null, validInterestList)
        );
    }

    // Functional opaque-box test: Null phone number throws exception
    @Test
    void testCreateReaderDetailsWithNullPhoneNumberThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ReaderDetails(validReaderNumber, validReader, validBirthDate, null,
                true, true, true, null, validInterestList)
        );
    }

    // Functional opaque-box test: GDPR consent false throws exception
    @Test
    void testCreateReaderDetailsWithoutGdprConsentThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            new ReaderDetails(validReaderNumber, validReader, validBirthDate, validPhoneNumber,
                false, true, true, null, validInterestList)
        );
    }

    // Functional opaque-box test: Apply patch updates birth date
    @Test
    void testApplyPatchUpdatesBirthDate() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        Long currentVersion = readerDetails.getVersion();
        
        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setBirthDate("1995-05-15");
        
        readerDetails.applyPatch(currentVersion, request, null, null);
        
        assertEquals("1995-5-15", readerDetails.getBirthDate().toString());
    }

    // Functional opaque-box test: Apply patch updates phone number
    @Test
    void testApplyPatchUpdatesPhoneNumber() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        Long currentVersion = readerDetails.getVersion();
        
        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setPhoneNumber("919999999");
        
        readerDetails.applyPatch(currentVersion, request, null, null);
        
        assertEquals("919999999", readerDetails.getPhoneNumber());
    }

    // Functional opaque-box test: Apply patch updates marketing consent
    @Test
    void testApplyPatchUpdatesMarketingConsent() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        Long currentVersion = readerDetails.getVersion();
        
        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setMarketing(false);
        
        readerDetails.applyPatch(currentVersion, request, null, null);
        
        assertFalse(readerDetails.isMarketingConsent());
    }

    // Functional opaque-box test: Apply patch updates reader name
    @Test
    void testApplyPatchUpdatesReaderName() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        Long currentVersion = readerDetails.getVersion();
        
        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setFullName("Jane Smith");
        
        readerDetails.applyPatch(currentVersion, request, null, null);
        
        assertEquals("Jane Smith", readerDetails.getReader().getName().toString());
    }

    // Functional opaque-box test: Apply patch with wrong version throws exception
    @Test
    void testApplyPatchWithWrongVersionThrowsException() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        
        UpdateReaderRequest request = new UpdateReaderRequest();
        request.setPhoneNumber("919999999");
        
        assertThrows(ConflictException.class, () ->
            readerDetails.applyPatch(999L, request, null, null)
        );
    }

    // Functional opaque-box test: Remove photo
    @Test
    void testRemovePhoto() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, "photo.jpg", validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        assertNotNull(readerDetails.getPhoto());
        
        Long currentVersion = readerDetails.getVersion();
        readerDetails.removePhoto(currentVersion);
        
        assertNull(readerDetails.getPhoto());
    }

    // Functional opaque-box test: Remove photo with wrong version throws exception
    @Test
    void testRemovePhotoWithWrongVersionThrowsException() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, "photo.jpg", validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        
        assertThrows(ConflictException.class, () ->
            readerDetails.removePhoto(999L)
        );
    }

    // Functional opaque-box test: Apply patch updates interest list
    @Test
    void testApplyPatchUpdatesInterestList() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        setVersion(readerDetails, 1L); // Simulate persisted entity
        Long currentVersion = readerDetails.getVersion();
        
        List<Genre> newInterestList = new ArrayList<>();
        newInterestList.add(new Genre("Science"));
        newInterestList.add(new Genre("History"));
        
        UpdateReaderRequest request = new UpdateReaderRequest();
        
        readerDetails.applyPatch(currentVersion, request, null, newInterestList);
        
        assertEquals(2, readerDetails.getInterestList().size());
    }

    // Functional opaque-box test: Get reader number returns correct format
    @Test
    void testGetReaderNumberReturnsCorrectFormat() {
        ReaderDetails readerDetails = new ReaderDetails(
            validReaderNumber, validReader, validBirthDate, validPhoneNumber,
            true, true, true, null, validInterestList
        );
        
        String readerNumber = readerDetails.getReaderNumber();
        assertNotNull(readerNumber);
        assertTrue(readerNumber.contains("" + validReaderNumber));
    }
}
