package pt.psoft.g1.psoftg1.bookmanagement.services;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of ISBN retrieval service using external APIs:
 * - Google Books API
 * - Open Library Search API
 */
@Slf4j
@Service
public class ExternalIsbnServiceImpl implements ExternalIsbnService {

    private final WebClient webClient;
    
    @Value("${external.api.googlebooks.url:https://www.googleapis.com/books/v1}")
    private String googleBooksUrl;
    
    @Value("${external.api.googlebooks.key:}")
    private String googleBooksApiKey;
    
    @Value("${external.api.openlibrary.url:https://openlibrary.org}")
    private String openLibraryUrl;
    
    @Value("${external.api.timeout:5000}")
    private long apiTimeout;

    public ExternalIsbnServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public List<IsbnSearchResult> searchIsbnByTitle(String title) {
        List<IsbnSearchResult> results = new ArrayList<>();
        
        // Call both APIs in parallel
        try {
            // Google Books API
            results.addAll(searchGoogleBooks(title));
        } catch (Exception e) {
            log.warn("Google Books API search failed for title '{}': {}", title, e.getMessage());
        }
        
        try {
            // Open Library API
            results.addAll(searchOpenLibrary(title));
        } catch (Exception e) {
            log.warn("Open Library API search failed for title '{}': {}", title, e.getMessage());
        }
        
        return results;
    }

    /**
     * Search Google Books API for books by title
     */
    private List<IsbnSearchResult> searchGoogleBooks(String title) {
        try {
            String uri = googleBooksUrl + "/volumes?q=intitle:" + title;
            if (googleBooksApiKey != null && !googleBooksApiKey.isEmpty()) {
                uri += "&key=" + googleBooksApiKey;
            }

            JsonNode response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofMillis(apiTimeout))
                    .onErrorResume(e -> {
                        log.error("Google Books API error: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response != null && response.has("items")) {
                JsonNode items = response.get("items");
                return StreamSupport.stream(items.spliterator(), false)
                        .limit(5) 
                        .map(item -> {
                            JsonNode volumeInfo = item.get("volumeInfo");
                            IsbnSearchResult result = new IsbnSearchResult();

                            if (volumeInfo.has("industryIdentifiers")) {
                                JsonNode identifiers = volumeInfo.get("industryIdentifiers");
                                for (JsonNode identifier : identifiers) {
                                    String type = identifier.get("type").asText();
                                    if ("ISBN_13".equals(type)) {
                                        result.setIsbn(identifier.get("identifier").asText());
                                        break;
                                    } else if ("ISBN_10".equals(type) && result.getIsbn() == null) {
                                        result.setIsbn(identifier.get("identifier").asText());
                                    }
                                }
                            }
                            
                            result.setTitle(volumeInfo.has("title") ? volumeInfo.get("title").asText() : null);
                            result.setSource("Google Books");
                            
                            // Extract authors
                            if (volumeInfo.has("authors")) {
                                List<String> authors = StreamSupport.stream(volumeInfo.get("authors").spliterator(), false)
                                        .map(JsonNode::asText)
                                        .collect(Collectors.toList());
                                result.setAuthors(authors);
                            }
                            
                            result.setPublishedDate(volumeInfo.has("publishedDate") ? 
                                    volumeInfo.get("publishedDate").asText() : null);
                            
                            return result;
                        })
                        .filter(r -> r.getIsbn() != null && !r.getIsbn().isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error parsing Google Books response: {}", e.getMessage());
        }
        
        return List.of();
    }

    /**
     * Search Open Library API for books by title
     */
    private List<IsbnSearchResult> searchOpenLibrary(String title) {
        try {
            JsonNode response = webClient.get()
                    .uri(openLibraryUrl + "/search.json?title=" + title + "&fields=title,author_name,isbn,first_publish_year&limit=5")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofMillis(apiTimeout))
                    .onErrorResume(e -> {
                        log.error("Open Library API error: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response != null && response.has("docs")) {
                JsonNode docs = response.get("docs");
                return StreamSupport.stream(docs.spliterator(), false)
                        .limit(5) // Limit to 5 results per source
                        .map(doc -> {
                            IsbnSearchResult result = new IsbnSearchResult();
                            
                            // Extract ISBN (prefer ISBN-13, fallback to ISBN-10)
                            if (doc.has("isbn")) {
                                JsonNode isbns = doc.get("isbn");
                                for (JsonNode isbn : isbns) {
                                    String isbnValue = isbn.asText();
                                    if (isbnValue.length() == 13) {
                                        result.setIsbn(isbnValue);
                                        break;
                                    } else if (isbnValue.length() == 10 && result.getIsbn() == null) {
                                        result.setIsbn(isbnValue);
                                    }
                                }
                            }
                            
                            result.setTitle(doc.has("title") ? doc.get("title").asText() : null);
                            result.setSource("Open Library");
                            
                            // Extract authors
                            if (doc.has("author_name")) {
                                List<String> authors = StreamSupport.stream(doc.get("author_name").spliterator(), false)
                                        .map(JsonNode::asText)
                                        .collect(Collectors.toList());
                                result.setAuthors(authors);
                            }
                            
                            result.setPublishedDate(doc.has("first_publish_year") ? 
                                    String.valueOf(doc.get("first_publish_year").asInt()) : null);
                            
                            return result;
                        })
                        .filter(r -> r.getIsbn() != null && !r.getIsbn().isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Error parsing Open Library response: {}", e.getMessage());
        }
        
        return List.of();
    }
}
