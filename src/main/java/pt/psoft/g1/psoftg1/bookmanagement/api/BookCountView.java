package pt.psoft.g1.psoftg1.bookmanagement.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Schema(description = "Books with lending count")
@NoArgsConstructor
@AllArgsConstructor
public class BookCountView implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotNull
    private BookView bookView;

    private Long lendingCount;
}
