package de.hs_mannheim.informatik.ct.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Veranstaltung {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    @ManyToOne(cascade = CascadeType.ALL)
    private Room room;
    private Date datum = new Date();
    private String angelegtVon;

    public Veranstaltung(String name, Room room, Date datum, String angelegtVon) {
        this.name = name;
        this.room = room;
        this.datum = datum;
        this.angelegtVon = angelegtVon;
    }

    public int getRaumkapazitaet() {
        return room.getMaxCapacity();
    }
}