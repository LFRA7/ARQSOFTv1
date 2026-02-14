package pt.psoft.g1.psoftg1.shared.model;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

import java.io.Serializable;

/**
 * Custom Hibernate ID generator that uses IdGenerationService.
 */
public class CustomIdGenerator implements IdentifierGenerator {
    
    private static IdGenerationService idGenerationService;
    
    /**
     * Static method to inject the service.
     * Called by ApplicationContextProvider during startup.
     */
    public static void setIdGenerationService(IdGenerationService service) {
        idGenerationService = service;
    }
    
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        if (idGenerationService == null) {
            throw new IllegalStateException("IdGenerationService has not been initialized.");
        }
        
        return idGenerationService.generateId();
    }
}
