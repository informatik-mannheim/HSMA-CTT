package de.hs_mannheim.informatik.ct.model;


import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomVisit {
    @ManyToOne
    @JoinColumn(updatable = false, insertable = false)
    private Room room;

    @Id
    @GeneratedValue
    private long id;

    @Column(updatable = false)
    private Date start;

    @Column
    @Setter
    private Date end = null;

    @ManyToOne
    @JoinColumn(updatable = false, insertable = false)
    private Besucher visitor;

    public RoomVisit(Besucher visitor, Room room, Date start) {
        this.visitor = visitor;
        this.room = room;
        this.start = start;
    }

    @lombok.Data
    @NoArgsConstructor
    public static class Data {
        private String roomId;
        private String visitorEmail;
        private Date start = null;
        private Date end = null;

        public Data(RoomVisit visit) {
            this.roomId = visit.room.getId();
            this.visitorEmail = visit.visitor.getEmail();
            this.start = visit.start;
            this.end = visit.end;
        }
    }
}
