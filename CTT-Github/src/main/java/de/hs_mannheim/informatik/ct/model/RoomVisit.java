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
    @JoinColumn
    @NonNull
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
    @JoinColumn
    @NonNull
    private Besucher visitor;

    public RoomVisit(Besucher visitor, Room room, Date start) {
        this.visitor = visitor;
        this.room = room;
        this.start = start;
    }

    @lombok.Data
    @NoArgsConstructor
    public static class Data {
        @NonNull
        private String roomId;
        @NonNull
        private String roomName;
        private int roomCapacity;
        private int currentVisitorCount;
        private String visitorEmail;
        private Date start = null;
        private Date end = null;

        public Data(RoomVisit visit, int currentVisitorCount) {
            this.roomId = visit.room.getId();
            this.roomName = visit.room.getName();
            this.roomCapacity = visit.room.getMaxCapacity();

            this.visitorEmail = visit.visitor.getEmail();
            this.currentVisitorCount = currentVisitorCount;
            this.start = visit.start;
            this.end = visit.end;
        }

        public Data(Room.Data roomData) {
            this.roomId = roomData.getRoomId();
            this.roomName = roomData.getRoomName();
            this.roomCapacity = roomData.getMaxCapacity();
        }
    }
}
