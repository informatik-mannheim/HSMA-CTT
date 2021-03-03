package de.hs_mannheim.informatik.ct.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Room {
    @Id
    @NonNull
    private String name;

    private String buildingName;

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
        @NonNull
        private String building;

        public Data(Room room) {
            roomName = room.getName();
            roomId = room.getId();
            maxCapacity = room.getMaxCapacity();
            building = room.getBuildingName();
        }
    }
}
