package pt.psoft.g1.psoftg1.shared.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.shared.model.CustomIdGenerator;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

/**
 * Provider to make Spring beans available to non-Spring managed classes.
 * Used to inject IdGenerationService into the CustomIdGenerator.
 */
@Component
public class ApplicationContextProvider {
    
    private final IdGenerationService idGenerationService;
    
    public ApplicationContextProvider(IdGenerationService idGenerationService) {
        this.idGenerationService = idGenerationService;
    }
    
    @PostConstruct
    public void init() {
        CustomIdGenerator.setIdGenerationService(idGenerationService);
    }
}
