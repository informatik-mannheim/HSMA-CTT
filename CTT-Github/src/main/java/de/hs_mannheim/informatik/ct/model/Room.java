package de.hs_mannheim.informatik.ct.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    @Id
    @NonNull
    private String name;

    @NonNull
    private int maxCapacity;

    public String getId() {
        return getName();
    }

    @lombok.Data
    @NoArgsConstructor
    public static class Data {
        @NonNull
        private String roomName;
        @NonNull
        private String roomId;
        @NonNull
        private int maxCapacity;

        public Data(Room room) {
            roomName = room.getName();
            roomId = room.getId();
            maxCapacity = room.getMaxCapacity();
        }
    }
}
