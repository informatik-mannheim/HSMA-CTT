package de.hs_mannheim.informatik.ct.persistence.services;

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
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.util.Optional;
import java.util.List;



@Service
public class RoomService {
    private final String COMMA_DELIMITER = ";";

    @Autowired
    private RoomRepository roomsRepo;

    public Optional<Room> findByName(String roomName) {
        return roomsRepo.findById(roomName);
    }

    public Room saveRoom(@NonNull Room room) {
        return roomsRepo.save(room);
    }

    @NonNull
    public List<Room> all() {
        return roomsRepo.findAll();
    }

    //TODO: Maybe check if csv is correctly formatted (or accept that the user uploads only correct files?)
    public void importFromCsv(BufferedReader csv) {
        csv.lines()
                .map((line) -> {
                    String[] values = line.split(COMMA_DELIMITER);
                    String building = values[0];
                    String roomName = values[1];
                    int roomCapacity = Integer.parseInt(values[2]);
                    return new Room(roomName, building, roomCapacity);
                })
                .forEach(this::saveRoom);
    }
}