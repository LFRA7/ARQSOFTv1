package pt.psoft.g1.psoftg1.bookmanagement.services;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

import java.io.Serializable;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCountDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Book book;
    private long lendingCount;
}
