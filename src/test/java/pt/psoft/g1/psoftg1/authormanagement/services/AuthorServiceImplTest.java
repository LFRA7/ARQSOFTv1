package pt.psoft.g1.psoftg1.authormanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorLendingView;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service Test - Unit testing with SUT = AuthorServiceImpl (mocked dependencies)
 * Tests the author service layer in isolation with mocked repositories
 */
@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorMapper mapper;

    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private Author validAuthor;
    private final Long authorNumber = 1L;

    @BeforeEach
    void setUp() {
        validAuthor = new Author("Test Author", "Author biography", null);
    }

    // Service test: Find all authors
    @Test
    void testFindAllAuthors() {
        List<Author> authors = List.of(validAuthor);
        when(authorRepository.findAll()).thenReturn(authors);

        Iterable<Author> result = authorService.findAll();

        assertNotNull(result);
        verify(authorRepository).findAll();
    }

    // Service test: Find by author number successfully
    @Test
    void testFindByAuthorNumberSuccessfully() {
        when(authorRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.of(validAuthor));

        Optional<Author> result = authorService.findByAuthorNumber(authorNumber);

        assertTrue(result.isPresent());
        assertEquals(validAuthor, result.get());
        verify(authorRepository).findByAuthorNumber(authorNumber);
    }

    // Service test: Find by author number not found
    @Test
    void testFindByAuthorNumberNotFound() {
        when(authorRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.empty());

        Optional<Author> result = authorService.findByAuthorNumber(authorNumber);

        assertFalse(result.isPresent());
        verify(authorRepository).findByAuthorNumber(authorNumber);
    }

    // Service test: Find by name
    @Test
    void testFindByName() {
        List<Author> authors = List.of(validAuthor);
        when(authorRepository.searchByNameNameStartsWith("Test")).thenReturn(authors);

        List<Author> result = authorService.findByName("Test");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(authorRepository).searchByNameNameStartsWith("Test");
    }

    // Service test: Create author successfully
    @Test
    void testCreateAuthorSuccessfully() {
        CreateAuthorRequest request = new CreateAuthorRequest("New Author", "New bio", null, null);
        when(mapper.create(request)).thenReturn(validAuthor);
        when(authorRepository.save(validAuthor)).thenReturn(validAuthor);

        Author result = authorService.create(request);

        assertNotNull(result);
        verify(mapper).create(request);
        verify(authorRepository).save(validAuthor);
    }

    // Service test: Partial update author successfully
    @Test
    void testPartialUpdateAuthorSuccessfully() {
        UpdateAuthorRequest request = new UpdateAuthorRequest("Updated Name", "Updated bio", null, null);
        
        when(authorRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.of(validAuthor));
        when(authorRepository.save(any(Author.class))).thenReturn(validAuthor);

        Author result = authorService.partialUpdate(authorNumber, request, 0L);

        assertNotNull(result);
        verify(authorRepository).findByAuthorNumber(authorNumber);
        verify(authorRepository).save(validAuthor);
    }

    // Service test: Partial update author not found throws exception
    @Test
    void testPartialUpdateAuthorNotFoundThrowsException() {
        UpdateAuthorRequest request = new UpdateAuthorRequest("Updated Name", "Updated bio", null, null);
        
        when(authorRepository.findByAuthorNumber(authorNumber)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> 
            authorService.partialUpdate(authorNumber, request, 0L));
        
        verify(authorRepository).findByAuthorNumber(authorNumber);
        verify(authorRepository, never()).save(any());
    }

    // Service test: Find books by author number
    @Test
    void testFindBooksByAuthorNumber() {
        Genre genre = new Genre("Fiction");
        Book book = new Book("9782826012092", "Test Book", "Description", 
            genre, List.of(validAuthor), null);
        List<Book> books = List.of(book);
        
        when(bookRepository.findBooksByAuthorNumber(authorNumber)).thenReturn(books);

        List<Book> result = authorService.findBooksByAuthorNumber(authorNumber);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookRepository).findBooksByAuthorNumber(authorNumber);
    }

    // Service test: Find books by author number returns empty list
    @Test
    void testFindBooksByAuthorNumberReturnsEmptyList() {
        when(bookRepository.findBooksByAuthorNumber(authorNumber)).thenReturn(new ArrayList<>());

        List<Book> result = authorService.findBooksByAuthorNumber(authorNumber);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(bookRepository).findBooksByAuthorNumber(authorNumber);
    }
}
