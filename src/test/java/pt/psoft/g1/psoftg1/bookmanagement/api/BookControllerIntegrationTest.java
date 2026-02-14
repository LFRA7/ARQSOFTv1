package pt.psoft.g1.psoftg1.bookmanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.configuration.TestSecurityConfig;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test - Functional opaque-box with SUT = BookController + BookService + Repository
 * Tests the entire book management flow from controller to database
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CacheManager cacheManager;

    private Genre testGenre;
    private Author testAuthor;
    private Book testBook;
    private final String testIsbn = "9782826012092";

    @BeforeEach
    void setUp() {
        // Clear all Redis caches before each test to avoid serialization issues
        Objects.requireNonNull(cacheManager.getCache("book")).clear();
        Objects.requireNonNull(cacheManager.getCache("booksList")).clear();
        
        // Setup test data
        testGenre = genreRepository.save(new Genre("Fiction"));
        testAuthor = authorRepository.save(new Author("Test Author", "Test Bio", null));
        testBook = bookRepository.save(new Book(testIsbn, "Test Book", "Test Description", 
            testGenre, List.of(testAuthor), null));
    }

    // Integration test: Get book by ISBN
    @Test
    @WithMockUser
    void testGetBookByIsbn() throws Exception {
        mockMvc.perform(get("/api/books/{isbn}", testIsbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(testIsbn))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    // Integration test: Get book by ISBN not found
    @Test
    @WithMockUser
    void testGetBookByIsbnNotFound() throws Exception {
        mockMvc.perform(get("/api/books/{isbn}", "9999999999999"))
                .andExpect(status().isNotFound());
    }

    // Integration test: Find books by title
    @Test
    @WithMockUser
    void testFindBooksByTitle() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("title", "Test Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.items[0].title").value("Test Book"));
    }

    // Integration test: Find books by genre
    @Test
    @WithMockUser
    void testFindBooksByGenre() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("genre", "Fiction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(1))));
    }

    // Integration test: Find books by author name
    @Test
    @WithMockUser
    void testFindBooksByAuthorName() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("authorName", "Test Author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(1))));
    }

    // Integration test: Get book photo when no photo exists
    @Test
    @WithMockUser
    void testGetBookPhotoWhenNoPhotoExists() throws Exception {
        mockMvc.perform(get("/api/books/{isbn}/photo", testIsbn))
                .andExpect(status().isOk());
    }

    // Integration test: Delete book photo when no photo exists
    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void testDeleteBookPhotoWhenNoPhotoExists() throws Exception {
        mockMvc.perform(delete("/api/books/{isbn}/photo", testIsbn))
                .andExpect(status().isNotFound());
    }

    // Integration test: Get top 5 books lent
    @Test
    @WithMockUser
    void testGetTop5BooksLent() throws Exception {
        mockMvc.perform(get("/api/books/top5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    // Integration test: Search books with query
    @Test
    @WithMockUser
    void testSearchBooksWithQuery() throws Exception {
        String searchRequest = """
                {
                    "page": {"number": 1, "limit": 10},
                    "query": {"title": "Test", "genre": "", "authorName": ""}
                }
                """;

        mockMvc.perform(post("/api/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    // Integration test: Get average lending duration for book
    @Test
    @WithMockUser
    void testGetAverageLendingDuration() throws Exception {
        mockMvc.perform(get("/api/books/{isbn}/avgDuration", testIsbn))
                .andExpect(status().isBadRequest());
    }
}
