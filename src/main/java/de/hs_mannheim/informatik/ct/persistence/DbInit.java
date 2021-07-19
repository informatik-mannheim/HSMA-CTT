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
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Service
public class DbInit implements CommandLineRunner {
    @Value("${server_env:production}")
    private String serverEnvironment;

    @Autowired
    private RoomService roomService;

    @Override
    public void run(String... args) {
        if (serverEnvironment.equals("dev")) {
            val roomList = new ArrayList<Room>();
//            val roomList = Arrays.asList(
//                    new Room("test", "test", 12),
//                    new Room("A007a", "A", 3),
//                    new Room("A210", "A", 19),
//                    new Room("A211", "A", 19),
//                    new Room("A212", "A", 19),
//                    new Room("A213", "A", 19),
//                    new Room("A214", "A", 19),
//                    new Room("A215", "A", 19),
//                    new Room("A216", "A", 19),
//                    new Room("A217", "A", 19),
//                    new Room("A218", "A", 19),
//                    new Room("B210", "B", 19),
//                    new Room("B211", "B", 19),
//                    new Room("B212", "B", 19),
//                    new Room("B213", "B", 19),
//                    new Room("B214", "B", 19),
//                    new Room("B215", "B", 19),
//                    new Room("B216", "B", 19),
//                    new Room("B217", "B", 19),
//                    new Room("C1", "C", 19),
//                    new Room("C4", "C", 19)
//            );

            for (int i = 0; i < 1200; i++) {
                roomList.add(new Room("Room " + i, "A", 22));
            }

            roomService.saveAllRooms(roomList);
        }
    }

}
