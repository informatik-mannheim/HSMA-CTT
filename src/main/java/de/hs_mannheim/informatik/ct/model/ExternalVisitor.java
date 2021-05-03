package de.hs_mannheim.informatik.ct.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity

@NoArgsConstructor
@Getter
@Setter
public class ExternalVisitor extends Visitor {
    @Column
    @NonNull
    private String name;
    @Column
    private String number;
    @Column
    private String address;

    public static ExternalVisitor visitorWithPhone(String email, String name, String number) {
        return new ExternalVisitor(email, name, number, null);
    }

    public static ExternalVisitor visitorWithAddress(String email, String name, String address) {
        return new ExternalVisitor(email, name, null, address);
    }

    public ExternalVisitor(String email, String name, String number, String address) {
        super(email);
        this.name = name;
        this.number = number;
        this.address = address;
    }

}
