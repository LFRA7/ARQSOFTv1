package pt.psoft.g1.psoftg1.shared.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.nio.file.Path;

@Entity
@Document("photos")
public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(generator = "custom-id-generator")
    @GenericGenerator(name = "custom-id-generator", 
                      strategy = "pt.psoft.g1.psoftg1.shared.model.CustomIdGenerator")
    private long pk;

    @NotNull
    @Setter
    @Getter
    private String photoFile;

    protected Photo (){}

    public Photo (Path photoPath){
        setPhotoFile(photoPath.toString());
    }
}

