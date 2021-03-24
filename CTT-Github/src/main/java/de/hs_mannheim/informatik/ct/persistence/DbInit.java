package de.hs_mannheim.informatik.ct.persistence;

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

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class DbInit implements CommandLineRunner {
    @Value("${server_env:production}")
    private String serverEnvironment;

    @Autowired
    private RoomRepository roomsRepo;


    // For testing uncomment (we dont read Rooms from csv, for testing we initialize them here)


/*    public Optional<Room> findByName(String roomName) {
        try {
            return roomsRepo.findByNameIgnoreCase(roomName);
        } catch (IncorrectResultSizeDataAccessException e) {
            return roomsRepo.findById(roomName);
        }
    }

    public Room saveRoom(@NonNull Room room) {
        System.out.println("Room Name this: " + room.getName());
        System.out.println("Room Name find: " + findByName(room.getName()).get().getName());

        if (findByName(room.getName()).get().getName().equals(room.getName())) {
            System.out.println("True");

            System.out.println("Room Pin before: " + room.getRoomPin());
            room.setRoomPin(findByName(room.getName()).get().getRoomPin());
            System.out.println("Room Pin after: " + room.getRoomPin());

        }
        return roomsRepo.save(room);
    }*/

    @Override
    public void run(String... args) {
        if (serverEnvironment.equals("dev")) {

            roomsRepo.save(new Room("A007a", "A", 3));
            roomsRepo.save(new Room("test", "test", 12));
            roomsRepo.save(new Room("A210", "A", 19));

            // saveRoom(new Room("A007a", "A", 3));
            // saveRoom(new Room("test", "test", 12));
            // saveRoom(new Room("A210", "A", 19));
        }
    }

}
