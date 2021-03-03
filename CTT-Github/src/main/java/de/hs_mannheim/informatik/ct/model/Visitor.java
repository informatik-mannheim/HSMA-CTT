package de.hs_mannheim.informatik.ct.model;

import de.hs_mannheim.informatik.ct.util.AttributeEncryptor;
import lombok.*;


import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = @Index(columnList = "email"))
public class Visitor {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    @NonNull
    @Convert(converter = AttributeEncryptor.class)
    private String email;

    public Visitor(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "{email='" + email + "'}";
    }
}
