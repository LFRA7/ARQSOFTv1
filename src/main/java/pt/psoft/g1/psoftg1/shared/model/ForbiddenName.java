package pt.psoft.g1.psoftg1.shared.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Entity
@Document("forbidden_names")
@NoArgsConstructor
public class ForbiddenName implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(generator = "custom-id-generator")
    @GenericGenerator(name = "custom-id-generator", 
                      strategy = "pt.psoft.g1.psoftg1.shared.model.CustomIdGenerator")
    private Long pk;

    @Getter
    @Setter
    @Column(nullable = false)
    @Size(min = 1)
    private String forbiddenName;

    public ForbiddenName(String name) {
        this.forbiddenName = name;
    }
}
