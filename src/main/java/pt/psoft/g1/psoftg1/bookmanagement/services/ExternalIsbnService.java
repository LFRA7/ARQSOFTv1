package pt.psoft.g1.psoftg1.bookmanagement.services;

import java.util.List;

/**
 * Service interface for retrieving ISBN information from external APIs
 */
public interface ExternalIsbnService {
    /**
     * Search for books by title across multiple external APIs
     * @param title The book title to search for
     * @return List of ISBN search results from different sources
     */
    List<IsbnSearchResult> searchIsbnByTitle(String title);
}
