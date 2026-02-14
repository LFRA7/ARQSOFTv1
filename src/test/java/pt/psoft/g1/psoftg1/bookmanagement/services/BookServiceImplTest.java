package pt.psoft.g1.psoftg1.bookmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.ConflictException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Service Test - Unit testing with SUT = BookServiceImpl (mocked dependencies)
 * Tests the service layer in isolation with mocked repositories
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private ReaderRepository readerRepository;

    @Mock
    private pt.psoft.g1.psoftg1.bookmanagement.api.BookViewMapper bookViewMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Genre validGenre;
    private Author validAuthor;
    private Book validBook;
    private final String validIsbn = "9782826012092";

    @BeforeEach
    void setUp() {
        validGenre = new Genre("Fiction");
        validAuthor = new Author("Test Author", "Bio", null);
        List<Author> authors = new ArrayList<>();
        authors.add(validAuthor);
        validBook = new Book(validIsbn, "Test Book", "Description", validGenre, authors, null);
        // Set version using ReflectionTestUtils to simulate persisted entity
        ReflectionTestUtils.setField(validBook, "version", 0L);
        
        ReflectionTestUtils.setField(bookService, "suggestionsLimitPerGenre", 5L);
    }
    
    // Helper method to set version
    private void setVersion(Book book, Long version) {
        ReflectionTestUtils.setField(book, "version", version);
    }

    // Service test: Create book successfully
    @Test
    void testCreateBookSuccessfully() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        request.setDescription("Description");
        request.setGenre("Fiction");
        request.setAuthors(List.of(1L));

        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.empty());
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(validGenre));
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(validAuthor));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        Book result = bookService.create(request, validIsbn);

        assertNotNull(result);
        verify(bookRepository).findByIsbn(validIsbn);
        verify(genreRepository).findByString("Fiction");
        verify(authorRepository).findByAuthorNumber(1L);
        verify(bookRepository).save(any(Book.class));
    }

    // Service test: Create book with existing ISBN throws exception
    @Test
    void testCreateBookWithExistingIsbnThrowsException() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        request.setGenre("Fiction");
        request.setAuthors(List.of(1L));

        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.of(validBook));

        assertThrows(ConflictException.class, () -> bookService.create(request, validIsbn));
        verify(bookRepository).findByIsbn(validIsbn);
        verify(bookRepository, never()).save(any());
    }

    // Service test: Create book with non-existent genre throws exception
    @Test
    void testCreateBookWithNonExistentGenreThrowsException() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        request.setGenre("NonExistent");
        request.setAuthors(List.of(1L));

        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.empty());
        when(genreRepository.findByString("NonExistent")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.create(request, validIsbn));
        verify(genreRepository).findByString("NonExistent");
        verify(bookRepository, never()).save(any());
    }

    // Service test: Find by ISBN successfully
    @Test
    void testFindByIsbnSuccessfully() {
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.of(validBook));

        Book result = bookService.findByIsbn(validIsbn);

        assertNotNull(result);
        assertEquals(validBook, result);
        verify(bookRepository).findByIsbn(validIsbn);
    }

    // Service test: Find by ISBN not found throws exception
    @Test
    void testFindByIsbnNotFoundThrowsException() {
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.findByIsbn(validIsbn));
        verify(bookRepository).findByIsbn(validIsbn);
    }

    // Service test: Find by genre
    @Test
    void testFindByGenre() {
        List<Book> books = List.of(validBook);
        when(bookRepository.findByGenre("Fiction")).thenReturn(books);

        List<Book> result = bookService.findByGenre("Fiction");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findByGenre("Fiction");
    }

    // Service test: Find by title
    @Test
    void testFindByTitle() {
        List<Book> books = List.of(validBook);
        when(bookRepository.findByTitle("Test Book")).thenReturn(books);

        List<Book> result = bookService.findByTitle("Test Book");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findByTitle("Test Book");
    }

    // Service test: Find by author name
    @Test
    void testFindByAuthorName() {
        List<Book> books = List.of(validBook);
        when(bookRepository.findByAuthorName("Test%")).thenReturn(books);

        List<Book> result = bookService.findByAuthorName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findByAuthorName("Test%");
    }

    // Service test: Update book successfully
    @Test
    void testUpdateBookSuccessfully() {
        UpdateBookRequest request = new UpdateBookRequest();
        request.setIsbn(validIsbn);
        request.setTitle("Updated Title");
        request.setGenre("Science");

        Genre newGenre = new Genre("Science");
        
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.of(validBook));
        when(genreRepository.findByString("Science")).thenReturn(Optional.of(newGenre));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        Book result = bookService.update(request, "0");

        assertNotNull(result);
        verify(bookRepository).findByIsbn(validIsbn);
        verify(genreRepository).findByString("Science");
        verify(bookRepository).save(validBook);
    }

    // Service test: Save book
    @Test
    void testSaveBook() {
        when(bookRepository.save(validBook)).thenReturn(validBook);

        Book result = bookService.save(validBook);

        assertNotNull(result);
        assertEquals(validBook, result);
        verify(bookRepository).save(validBook);
    }

    // Service test: Find top 5 books lent
    @Test
    void testFindTop5BooksLent() {
        BookCountDTO dto = new BookCountDTO() {
            @Override
            public Book getBook() {
                return validBook;
            }

            @Override
            public long getLendingCount() {
                return 10L;
            }
        };
        
        Page<BookCountDTO> page = new PageImpl<>(List.of(dto));
        when(bookRepository.findTop5BooksLent(any(LocalDate.class), any(Pageable.class))).thenReturn(page);
        
        // Mock BookViewMapper
        pt.psoft.g1.psoftg1.bookmanagement.api.BookView mockBookView = new pt.psoft.g1.psoftg1.bookmanagement.api.BookView();
        mockBookView.setTitle("Test Book");
        mockBookView.setIsbn(validIsbn);
        when(bookViewMapper.toBookView(any(Book.class))).thenReturn(mockBookView);

        List<pt.psoft.g1.psoftg1.bookmanagement.api.BookCountView> result = bookService.findTop5BooksLent();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findTop5BooksLent(any(LocalDate.class), any(Pageable.class));
        verify(bookViewMapper).toBookView(any(Book.class));
    }

    // Service test: Remove book photo successfully
    @Test
    void testRemoveBookPhotoSuccessfully() {
        Book bookWithPhoto = new Book(validIsbn, "Test Book", "Description", validGenre, 
            List.of(validAuthor), "photo.jpg");
        setVersion(bookWithPhoto, 0L); // Simulate persisted entity
        
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.of(bookWithPhoto));
        when(bookRepository.save(any(Book.class))).thenReturn(bookWithPhoto);
        doNothing().when(photoRepository).deleteByPhotoFile("photo.jpg");

        Book result = bookService.removeBookPhoto(validIsbn, 0L);

        assertNotNull(result);
        verify(bookRepository).findByIsbn(validIsbn);
        verify(bookRepository).save(bookWithPhoto);
        verify(photoRepository).deleteByPhotoFile("photo.jpg");
    }

    // Service test: Remove book photo when no photo exists throws exception
    @Test
    void testRemoveBookPhotoWhenNoPhotoThrowsException() {
        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.of(validBook));

        assertThrows(NotFoundException.class, () -> bookService.removeBookPhoto(validIsbn, 0L));
        verify(bookRepository).findByIsbn(validIsbn);
        verify(photoRepository, never()).deleteByPhotoFile(anyString());
    }

    // Service test: Get book suggestions for reader
    @Test
    void testGetBooksSuggestionsForReader() {
        Reader reader = Reader.newReader("test@test.com", "Pass123!", "Test User");
        List<Genre> interests = new ArrayList<>();
        interests.add(validGenre);
        ReaderDetails readerDetails = new ReaderDetails(1, reader, "2000-01-01", "912345678",
            true, true, true, null, interests);

        when(readerRepository.findByReaderNumber(anyString())).thenReturn(Optional.of(readerDetails));
        when(bookRepository.findByGenre("Fiction")).thenReturn(List.of(validBook));

        List<Book> result = bookService.getBooksSuggestionsForReader("2024/1");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(readerRepository).findByReaderNumber(anyString());
        verify(bookRepository).findByGenre("Fiction");
    }

    // Service test: Get book suggestions when reader not found throws exception
    @Test
    void testGetBooksSuggestionsWhenReaderNotFoundThrowsException() {
        when(readerRepository.findByReaderNumber(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getBooksSuggestionsForReader("2024/1"));
        verify(readerRepository).findByReaderNumber(anyString());
    }

    // Service test: Get book suggestions when reader has no interests throws exception
    @Test
    void testGetBooksSuggestionsWhenNoInterestsThrowsException() {
        Reader reader = Reader.newReader("test@test.com", "Pass123!", "Test User");
        ReaderDetails readerDetails = new ReaderDetails(1, reader, "2000-01-01", "912345678",
            true, true, true, null, new ArrayList<>());

        when(readerRepository.findByReaderNumber(anyString())).thenReturn(Optional.of(readerDetails));

        assertThrows(NotFoundException.class, () -> bookService.getBooksSuggestionsForReader("2024/1"));
        verify(readerRepository).findByReaderNumber(anyString());
    }

    // Service test: Search books with null parameters uses defaults
    @Test
    void testSearchBooksWithNullParametersUsesDefaults() {
        List<Book> books = List.of(validBook);
        when(bookRepository.searchBooks(any(pt.psoft.g1.psoftg1.shared.services.Page.class), 
            any(SearchBooksQuery.class))).thenReturn(books);

        List<Book> result = bookService.searchBooks(null, null);

        assertNotNull(result);
        verify(bookRepository).searchBooks(any(pt.psoft.g1.psoftg1.shared.services.Page.class), 
            any(SearchBooksQuery.class));
    }

    // Service test: Search books with valid parameters
    @Test
    void testSearchBooksWithValidParameters() {
        pt.psoft.g1.psoftg1.shared.services.Page page = new pt.psoft.g1.psoftg1.shared.services.Page(1, 10);
        SearchBooksQuery query = new SearchBooksQuery("Test", "Fiction", "Author");
        List<Book> books = List.of(validBook);
        
        when(bookRepository.searchBooks(page, query)).thenReturn(books);

        List<Book> result = bookService.searchBooks(page, query);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).searchBooks(page, query);
    }

    // Service test: Create book with multiple authors
    @Test
    void testCreateBookWithMultipleAuthors() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        request.setGenre("Fiction");
        request.setAuthors(List.of(1L, 2L));

        Author author2 = new Author("Second Author", "Bio 2", null);

        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.empty());
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(validGenre));
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(validAuthor));
        when(authorRepository.findByAuthorNumber(2L)).thenReturn(Optional.of(author2));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        Book result = bookService.create(request, validIsbn);

        assertNotNull(result);
        verify(authorRepository).findByAuthorNumber(1L);
        verify(authorRepository).findByAuthorNumber(2L);
    }

    // Service test: Create book skips non-existent authors
    @Test
    void testCreateBookSkipsNonExistentAuthors() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("New Book");
        request.setGenre("Fiction");
        request.setAuthors(List.of(1L, 999L));

        when(bookRepository.findByIsbn(validIsbn)).thenReturn(Optional.empty());
        when(genreRepository.findByString("Fiction")).thenReturn(Optional.of(validGenre));
        when(authorRepository.findByAuthorNumber(1L)).thenReturn(Optional.of(validAuthor));
        when(authorRepository.findByAuthorNumber(999L)).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        Book result = bookService.create(request, validIsbn);

        assertNotNull(result);
        verify(authorRepository).findByAuthorNumber(1L);
        verify(authorRepository).findByAuthorNumber(999L);
    }
}
