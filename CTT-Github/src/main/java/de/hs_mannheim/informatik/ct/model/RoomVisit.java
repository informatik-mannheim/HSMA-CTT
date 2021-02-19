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
    private Date startDate;

    @Column
    @Setter
    private Date endDate = null;

    @ManyToOne
    @JoinColumn
    @NonNull
    private Visitor visitor;

    public RoomVisit(Visitor visitor, Room room, Date startDate) {
        this.visitor = visitor;
        this.room = room;
        this.startDate = startDate;
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
        private Date startDate = null;
        private Date endDate = null;

        public Data(RoomVisit visit, int currentVisitorCount) {
            this.roomId = visit.room.getId();
            this.roomName = visit.room.getName();
            this.roomCapacity = visit.room.getMaxCapacity();

            this.visitorEmail = visit.visitor.getEmail();
            this.currentVisitorCount = currentVisitorCount;
            this.startDate = visit.startDate;
            this.endDate = visit.endDate;
        }

        public Data(Room.Data roomData) {
            this.roomId = roomData.getRoomId();
            this.roomName = roomData.getRoomName();
            this.roomCapacity = roomData.getMaxCapacity();
        }
    }
}
