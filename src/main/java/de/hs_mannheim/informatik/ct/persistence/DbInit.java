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

package de.hs_mannheim.informatik.ct.persistence;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import lombok.val;

@Service
public class DbInit implements CommandLineRunner {
    @Value("${server_env:production}")
    private String serverEnvironment;

    @Autowired
    private RoomService roomService;

    @Override
    public void run(String... args) {
        if (serverEnvironment.equalsIgnoreCase("dev")) {
            val roomList = Arrays.asList(
                    new Room("A007a", "A", 3),
                    new Room("test", "test", 12),
                    new Room("A210", "A", 19)
            );

            roomService.saveAllRooms(roomList);
        }
    }
}
