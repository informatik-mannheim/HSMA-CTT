package de.hs_mannheim.informatik.ct.model;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


import lombok.*;

import javax.persistence.*;
import java.util.Date;


@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoomVisit implements Visit {
    @ManyToOne
    @JoinColumn
    @NonNull
    private Room room;

    @Id
    @GeneratedValue
    private Long id;

    @Column(updatable = false)
    private Date startDate;

    @Column
    private Date endDate = null;

    @ManyToOne
    @JoinColumn
    @NonNull
    private Visitor visitor;

    @Column
    @Enumerated
    private CheckOutSource checkOutSource = CheckOutSource.NotCheckedOut;

    public RoomVisit(@NonNull Visitor visitor, @NonNull Room room, Date startDate) {
        this.visitor = visitor;
        this.room = room;
        this.startDate = startDate;
    }

    @Override
    public String getLocationName() {
        return room.getName();
    }

    public void checkOut(@NonNull Date checkOutDate, @NonNull CheckOutSource reason) {
        // normal check out
        // or enddate was set but user did not got checked out
        if (endDate == null && reason != CheckOutSource.NotCheckedOut) {
            endDate = checkOutDate;
            checkOutSource = reason;
        } else if (checkOutSource == CheckOutSource.NotCheckedOut) {
            checkOutSource = CheckOutSource.AutomaticCheckout;
        }
    }

    public CheckOutSource getCheckOutSource() {
        assert endDate != null || checkOutSource == CheckOutSource.NotCheckedOut;

        return checkOutSource;
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
        private String name;
        private String address;
        private String number;

        private String roomPin;
        private boolean privileged;

        public Data(RoomVisit visit, int currentVisitorCount) {

            if (visit.visitor instanceof ExternalVisitor) {
                var externalVisitor = (ExternalVisitor) visit.visitor;
                this.address = externalVisitor.getAddress();
                this.name = externalVisitor.getName();
                this.number = externalVisitor.getNumber();
            }

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
