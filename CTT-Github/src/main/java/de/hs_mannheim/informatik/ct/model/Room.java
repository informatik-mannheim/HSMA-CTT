package de.hs_mannheim.informatik.ct.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.regex.Pattern;

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

    private int maxCapacity;

    public String getId() {
        return getName();
    }

    public String getBuildingName() {
        // TODO: Store actual building names in db
        // Matches rooms like A008 or A007a, which have the building as the initial letter
        val roomWithBuildingMatcher = Pattern.compile("[A-Z]\\d+\\w?");
        if(roomWithBuildingMatcher.matcher(name).matches()) {
            return name.substring(0, 1);
        } else {
            return "?";
        }
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
