/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.persistence.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;

@Service
public class BuildingService {
    @Autowired
    private RoomRepository roomRepository;

    public List<String> getAllBuildings() {
        return roomRepository.getAllBuildings();
    }

    public List<Room> getAllRoomsInBuilding(String building) {
        return roomRepository.findByBuildingName(building);
    }
    
    public List<Room> getAllRooms(){
        List<Room> allRooms = new ArrayList<>();
       List<String> allBuildings = getAllBuildings();
       for (String building: allBuildings){
           List<Room> roomsinBuilding = getAllRoomsInBuilding(building);
           for (Room room: roomsinBuilding){
               allRooms.add(room);
           }
       }
       return allRooms;
    }
}
