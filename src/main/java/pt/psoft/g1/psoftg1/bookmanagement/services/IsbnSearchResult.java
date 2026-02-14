package pt.psoft.g1.psoftg1.bookmanagement.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for ISBN search results from external APIs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IsbnSearchResult {
    private String isbn;
    private String title;
    private String source; // Google Books or Open Library
    private List<String> authors;
    private String publishedDate;
}
