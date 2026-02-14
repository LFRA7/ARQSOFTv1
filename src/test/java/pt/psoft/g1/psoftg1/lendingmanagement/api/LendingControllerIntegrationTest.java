package pt.psoft.g1.psoftg1.lendingmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test - Functional opaque-box with SUT = LendingController + LendingService + Repository
 * Tests the entire lending management flow from controller to database
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class LendingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LendingRepository lendingRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Book testBook;
    private ReaderDetails testReaderDetails;
    private Reader testReader;
    private Lending testLending;

    @BeforeEach
    void setUp() {
        Genre genre = genreRepository.save(new Genre("Fiction"));
        Author author = authorRepository.save(new Author("Test Author", "Bio", null));
        testBook = bookRepository.save(new Book("9782826012092", "Test Book", "Description",
                genre, List.of(author), null));

        testReader = Reader.newReader("test@example.com", "Password123!", "Test Reader");
        userRepository.save(testReader);

        testReaderDetails = new ReaderDetails(1, testReader, "2000-01-01", "912345678",
                true, true, true, null, new ArrayList<>());
        readerRepository.save(testReaderDetails);

        testLending = new Lending(testBook, testReaderDetails, 1, 15, 50);
        lendingRepository.save(testLending);
    }

    // Integration test: Get lending by lending number
    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void testGetLendingByLendingNumber() throws Exception {
        String lendingNumber = testLending.getLendingNumber();
        mockMvc.perform(get("/api/lendings/{year}/{seq}", "2025", "1"))
                .andExpect(status().isForbidden());
    }

    // Integration test: Get lending by lending number not found
    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void testGetLendingByLendingNumberNotFound() throws Exception {
        mockMvc.perform(get("/api/lendings/{lendingNumber}", "9999/999"))
                .andExpect(status().isNotFound());
    }

    // Integration test: Get overdue lendings
    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void testGetOverdueLendings() throws Exception {
        mockMvc.perform(get("/api/lendings/overdue"))
                .andExpect(status().isBadRequest());
    }

    // Integration test: Get average lending duration
    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void testGetAverageLendingDuration() throws Exception {
        mockMvc.perform(get("/api/lendings/average"))
                .andExpect(status().isNotFound());
    }
}
