package pt.psoft.g1.psoftg1.configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

import java.lang.reflect.Field;

/**
 * MongoDB event listener that automatically generates IDs for entities before saving.
 * Uses the custom IdGenerationService to generate Long IDs.
 * 
 * This ensures MongoDB entities use the same ID generation strategy as JPA entities.
 */
@Component
@Profile({"mongodb-redis", "mongotest"})
public class MongoIdGenerationListener extends AbstractMongoEventListener<Object> {
    
    private final IdGenerationService idGenerationService;
    
    public MongoIdGenerationListener(IdGenerationService idGenerationService) {
        this.idGenerationService = idGenerationService;
    }
    
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object entity = event.getSource();
        generateIdIfNeeded(entity);
    }
    
    private void generateIdIfNeeded(Object entity) {
        try {
            // Look for a field named 'pk' or 'id' with type Long
            Field pkField = findIdField(entity.getClass());
            
            if (pkField != null) {
                pkField.setAccessible(true);
                Object currentValue = pkField.get(entity);

                if (currentValue == null || (currentValue instanceof Long && ((Long) currentValue) == 0)) {
                    Long generatedId = idGenerationService.generateId();
                    pkField.set(entity, generatedId);
                    System.out.println("Generated ID " + generatedId + " for entity " + entity.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ID for entity: " + entity.getClass().getName(), e);
        }
    }
    
    /**
     * Find the ID field in the entity class hierarchy.
     * Looks for fields named 'pk' or 'id' with type Long.
     */
    private Field findIdField(Class<?> clazz) {
        try {
            Field pkField = clazz.getDeclaredField("pk");
            if (pkField.getType().equals(Long.class) || pkField.getType().equals(long.class)) {
                return pkField;
            }
        } catch (NoSuchFieldException e) {
            
        }
        
        // Try to find 'id' field
        try {
            Field idField = clazz.getDeclaredField("id");
            if (idField.getType().equals(Long.class) || idField.getType().equals(long.class)) {
                return idField;
            }
        } catch (NoSuchFieldException e) {
            
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            return findIdField(superclass);
        }
        
        return null;
    }
}
