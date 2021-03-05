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
