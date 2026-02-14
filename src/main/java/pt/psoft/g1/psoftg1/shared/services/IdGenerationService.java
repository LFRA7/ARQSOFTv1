package pt.psoft.g1.psoftg1.shared.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for generating unique Long IDs.
 * Uses timestamp + counter to guarantee uniqueness even when called multiple times in same millisecond.
 * 
 * Format: [timestamp in milliseconds][4-digit counter]
 * - Timestamp: Current time in milliseconds
 * - Counter: Increments for IDs generated in same millisecond (0-9999)
 */
@Service
public class IdGenerationService {
    
    private final AtomicLong lastTimestamp = new AtomicLong(0L);
    private final AtomicLong counter = new AtomicLong(0L);
    
    @Value("${id.generation.strategy:DIGIT_SUFFIX_TIMESTAMP}")
    private String strategy;
    
    private boolean useDigitSuffixTimestamp = true;
    
    @PostConstruct
    public void init() {
        useDigitSuffixTimestamp = !"TIMESTAMP_DIGIT_SUFFIX".equalsIgnoreCase(strategy);
        System.out.println("ID Generation Strategy initialized: " + 
            (useDigitSuffixTimestamp ? "DIGIT_SUFFIX_TIMESTAMP" : "TIMESTAMP_DIGIT_SUFFIX"));
    }
    
    /**
     * Generates a unique Long ID.
     * Thread-safe and guarantees no duplicates.
     */
    public synchronized Long generateId() {
        long timestamp = Instant.now().toEpochMilli();
        
        // Check if we're in the same millisecond
        long last = lastTimestamp.get();
        if (timestamp == last) {
            long count = counter.incrementAndGet();
            if (count >= 10000) {
                while (timestamp == last) {
                    timestamp = Instant.now().toEpochMilli();
                }
                counter.set(0);
            }
        } else {
            lastTimestamp.set(timestamp);
            counter.set(0);
        }
        
        long currentCounter = counter.get();
        
        if (useDigitSuffixTimestamp) {
            // Format: [4-digit counter][timestamp]
            return currentCounter * 10_000_000_000_000L + timestamp;
        } else {
            // Format: [timestamp][4-digit counter]
            return timestamp * 10_000L + currentCounter;
        }
    }

    public String getStrategyName() {
        return useDigitSuffixTimestamp ? "DIGIT_SUFFIX_TIMESTAMP" : "TIMESTAMP_DIGIT_SUFFIX";
    }
}
