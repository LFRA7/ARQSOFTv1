package pt.psoft.g1.psoftg1.bookmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "A Book")
public class BookView implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull
    private String title;

    @NotNull
    private List<String> authors;

    @NotNull
    private String genre;

    private String description;

    @NotNull
    private String isbn;

    @Setter
    @Getter
    private Map<String, Object> _links = new HashMap<>();
}
