package pt.psoft.g1.psoftg1.shared.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify ID generation works correctly.
 */
class IdGenerationServiceTest {
    
    @Test
    void testGenerateIdProducesValidLong() {
        IdGenerationService service = new IdGenerationService();
        service.init(); // Initialize the service
        
        Long id1 = service.generateId();
        Long id2 = service.generateId();

        assertNotNull(id1);
        assertNotNull(id2);

        assertNotEquals(id1, id2, "IDs should be unique");
        
        // Verify they are positive and have reasonable length (at least 13 digits for timestamp)
        assertTrue(id1 > 0, "ID should be positive");
        assertTrue(id2 > 0, "ID should be positive");
        assertTrue(id1.toString().length() >= 13, "ID should be at least 13 digits (timestamp), but was: " + id1.toString().length());
        assertTrue(id2.toString().length() >= 13, "ID should be at least 13 digits (timestamp), but was: " + id2.toString().length());
        
        System.out.println("Generated ID 1: " + id1);
        System.out.println("Generated ID 2: " + id2);
        System.out.println("Strategy: " + service.getStrategyName());
    }
    
    @Test
    void testIdUniqueness() {
        IdGenerationService service = new IdGenerationService();
        
        java.util.Set<Long> ids = new java.util.HashSet<>();
        
        for (int i = 0; i < 100; i++) {
            Long id = service.generateId();
            assertTrue(ids.add(id), "Duplicate ID found: " + id);
        }
        
        assertEquals(100, ids.size(), "Should have 100 unique IDs");
        System.out.println("Successfully generated 100 unique IDs");
    }
}
