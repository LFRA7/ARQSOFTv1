package pt.psoft.g1.psoftg1.bookmanagement.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExternalIsbnServiceImpl
 * Tests ISBN search functionality using external APIs (Google Books and Open Library)
 */
@ExtendWith(MockitoExtension.class)
class ExternalIsbnServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ExternalIsbnServiceImpl externalIsbnService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        externalIsbnService = new ExternalIsbnServiceImpl(webClientBuilder);
        
        // Set test values using reflection
        ReflectionTestUtils.setField(externalIsbnService, "googleBooksUrl", "https://www.googleapis.com/books/v1");
        ReflectionTestUtils.setField(externalIsbnService, "googleBooksApiKey", "");
        ReflectionTestUtils.setField(externalIsbnService, "openLibraryUrl", "https://openlibrary.org");
        ReflectionTestUtils.setField(externalIsbnService, "apiTimeout", 5000L);
        
        objectMapper = new ObjectMapper();
    }

    /**
     * Service Unit Test
     * Tests successful ISBN search from Google Books API with ISBN-13
     */
    @Test
    void testSearchIsbnByTitle_GoogleBooks_Success() throws Exception {
        String title = "Clean Code";
        String googleBooksResponse = """
            {
                "items": [
                    {
                        "volumeInfo": {
                            "title": "Clean Code",
                            "authors": ["Robert C. Martin"],
                            "industryIdentifiers": [
                                {
                                    "type": "ISBN_13",
                                    "identifier": "9780132350884"
                                }
                            ],
                            "publishedDate": "2008-08-01"
                        }
                    }
                ]
            }
            """;
        
        JsonNode jsonResponse = objectMapper.readTree(googleBooksResponse);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(jsonResponse));
        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        
        IsbnSearchResult result = results.get(0);
        assertEquals("9780132350884", result.getIsbn());
        assertEquals("Clean Code", result.getTitle());
        assertEquals("Google Books", result.getSource());
        assertEquals("2008-08-01", result.getPublishedDate());
        assertNotNull(result.getAuthors());
        assertEquals(1, result.getAuthors().size());
        assertEquals("Robert C. Martin", result.getAuthors().get(0));

        verify(webClient, atLeastOnce()).get();
    }

    /**
     * Service Unit Test
     * Tests ISBN search with ISBN-10 fallback when ISBN-13 not available
     */
    @Test
    void testSearchIsbnByTitle_GoogleBooks_ISBN10Fallback() throws Exception {
        String title = "Java Programming";
        String googleBooksResponse = """
            {
                "items": [
                    {
                        "volumeInfo": {
                            "title": "Java Programming",
                            "authors": ["John Doe"],
                            "industryIdentifiers": [
                                {
                                    "type": "ISBN_10",
                                    "identifier": "0132350882"
                                }
                            ],
                            "publishedDate": "2010"
                        }
                    }
                ]
            }
            """;
        
        JsonNode jsonResponse = objectMapper.readTree(googleBooksResponse);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(jsonResponse));

        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("0132350882", results.get(0).getIsbn());
        assertEquals("Google Books", results.get(0).getSource());
    }

    /**
     * Service Unit Test
     * Tests successful ISBN search from Open Library API
     */
    @Test
    void testSearchIsbnByTitle_OpenLibrary_Success() throws Exception {
        String title = "Design Patterns";
        String openLibraryResponse = """
            {
                "docs": [
                    {
                        "title": "Design Patterns",
                        "author_name": ["Erich Gamma", "Richard Helm"],
                        "isbn": ["9780201633610", "0201633612"],
                        "first_publish_year": 1994
                    }
                ]
            }
            """;
        
        JsonNode jsonResponse = objectMapper.readTree(openLibraryResponse);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
            .thenReturn(Mono.error(new RuntimeException("Google Books failed")))
            .thenReturn(Mono.just(jsonResponse));

        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        IsbnSearchResult result = results.get(0);
        assertEquals("9780201633610", result.getIsbn()); // Prefers ISBN-13
        assertEquals("Design Patterns", result.getTitle());
        assertEquals("Open Library", result.getSource());
        assertEquals("1994", result.getPublishedDate());
        assertEquals(2, result.getAuthors().size());
    }

    /**
     * Service Unit Test
     * Tests that both APIs are called and results are combined
     */
    @Test
    void testSearchIsbnByTitle_BothAPIs_CombinedResults() throws Exception {
        String title = "Spring in Action";
        String googleResponse = """
            {
                "items": [
                    {
                        "volumeInfo": {
                            "title": "Spring in Action",
                            "authors": ["Craig Walls"],
                            "industryIdentifiers": [
                                {"type": "ISBN_13", "identifier": "9781617294945"}
                            ],
                            "publishedDate": "2018"
                        }
                    }
                ]
            }
            """;
        
        String openLibraryResponse = """
            {
                "docs": [
                    {
                        "title": "Spring Framework",
                        "author_name": ["Rod Johnson"],
                        "isbn": ["9780596009205"],
                        "first_publish_year": 2005
                    }
                ]
            }
            """;
        
        JsonNode googleJson = objectMapper.readTree(googleResponse);
        JsonNode openLibraryJson = objectMapper.readTree(openLibraryResponse);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
            .thenReturn(Mono.just(googleJson))
            .thenReturn(Mono.just(openLibraryJson));

        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertEquals(2, results.size()); 

        assertTrue(results.stream().anyMatch(r -> "Google Books".equals(r.getSource())));
        assertTrue(results.stream().anyMatch(r -> "Open Library".equals(r.getSource())));
    }

    /**
     * Service Unit Test
     * Tests handling of empty response from Google Books
     */
    @Test
    void testSearchIsbnByTitle_EmptyResponse() throws Exception {
        String title = "NonExistentBook";
        String emptyResponse = """
            {
                "items": []
            }
            """;
        
        JsonNode jsonResponse = objectMapper.readTree(emptyResponse);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(jsonResponse));

        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    /**
     * Service Unit Test
     * Tests handling of API errors with graceful degradation
     */
    @Test
    void testSearchIsbnByTitle_APIError_GracefulDegradation() {
        String title = "Test Book";
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
            .thenReturn(Mono.error(new RuntimeException("API Error")));

        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertTrue(results.isEmpty()); // Should return empty list, not throw exception
    }

    /**
     * Service Unit Test
     * Tests filtering of results without ISBNs
     */
    @Test
    void testSearchIsbnByTitle_FiltersResultsWithoutISBN() throws Exception {

        String title = "Book Without ISBN";
        String googleResponse = """
            {
                "items": [
                    {
                        "volumeInfo": {
                            "title": "Book Without ISBN",
                            "authors": ["Unknown Author"],
                            "publishedDate": "2020"
                        }
                    }
                ]
            }
            """;
        
        JsonNode jsonResponse = objectMapper.readTree(googleResponse);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(jsonResponse));

        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertTrue(results.isEmpty()); // Should filter out items without ISBN
    }

    /**
     * Service Unit Test
     * Tests handling of multiple books with limit of 5 per source
     */
    @Test
    void testSearchIsbnByTitle_LimitsResultsTo5PerSource() throws Exception {
        
        String title = "Java";
        StringBuilder itemsBuilder = new StringBuilder();
        itemsBuilder.append("{\"items\": [");
        
        // Create 10 items to test the limit
        for (int i = 0; i < 10; i++) {
            if (i > 0) itemsBuilder.append(",");
            itemsBuilder.append("""
                {
                    "volumeInfo": {
                        "title": "Java Book %d",
                        "industryIdentifiers": [
                            {"type": "ISBN_13", "identifier": "978012345678%d"}
                        ]
                    }
                }
                """.formatted(i, i));
        }
        itemsBuilder.append("]}");
        
        JsonNode jsonResponse = objectMapper.readTree(itemsBuilder.toString());
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(jsonResponse));

        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertTrue(results.size() <= 5, "Should limit to 5 results from Google Books");
    }

    /**
     * Service Unit Test
     * Tests Open Library prefers ISBN-13 over ISBN-10
     */
    @Test
    void testSearchIsbnByTitle_OpenLibrary_PrefersISBN13() throws Exception {
        String title = "Test Book";
        String openLibraryResponse = """
            {
                "docs": [
                    {
                        "title": "Test Book",
                        "author_name": ["Test Author"],
                        "isbn": ["0123456789", "9781234567890"],
                        "first_publish_year": 2020
                    }
                ]
            }
            """;
        
        JsonNode jsonResponse = objectMapper.readTree(openLibraryResponse);
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class))
            .thenReturn(Mono.error(new RuntimeException("Google error")))
            .thenReturn(Mono.just(jsonResponse));

        
        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("9781234567890", results.get(0).getIsbn()); // Should prefer 13-digit ISBN
    }

    /**
     * Service Unit Test
     * Tests that service handles null responses gracefully
     */
    @Test
    void testSearchIsbnByTitle_NullResponse_ReturnsEmptyList() {
        
        String title = "Test";
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.empty());

        
        List<IsbnSearchResult> results = externalIsbnService.searchIsbnByTitle(title);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
