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

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(cascade = {CascadeType.ALL})
    private List<VeranstaltungsBesuch> besuche = new ArrayList<>();

    public Veranstaltung(String name, Room room, Date datum, String angelegtVon) {
        this.name = name;
        this.room = room;
        this.datum = datum;
        this.angelegtVon = angelegtVon;
    }

    public Veranstaltung(Long id, String name, Room room, Date datum, String angelegtVon) {
        this(name, room, datum, angelegtVon);
        this.id = id;
    }

    public void addBesucher(Visitor b) {
        VeranstaltungsBesuch vb = new VeranstaltungsBesuch(this, b);
        besuche.add(vb);
        b.addVeranstaltungsBesuch(vb);
    }

    public int getRaumkapazitaet() {
        return room.getMaxCapacity();
    }
}
