package de.hs_mannheim.informatik.ct;

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

import com.sun.istack.NotNull;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoomServiceHelper {

    private final String COMMA_DELIMITER = ";";

    /**
     * Helper Method to load excel file
     * @param testExcelPath Filename of excel file. Needs to be stored in resources folder
     * @return InputStream containing data as if it was loaded from an existing excel file
     */
    public InputStream inputStreamFromTestExcel(String testExcelPath) throws IOException {
        File excelFile = new ClassPathResource(testExcelPath).getFile();
        InputStream is = new FileInputStream(excelFile);
        return is;
    }

    /**
     * Helper Method to create Buffered Reader from Array List. This is used to test a room import
     * feature without generating and reading from a csv file.
     *
     * @aparam roomData Arraylist holding room data.
     */
    public BufferedReader createCsvBuffer(@NotNull List<String[]> roomData) {
        StringBuilder buffer = new StringBuilder();
        String[] last = roomData.get(0);
        roomData.remove(last);

        if(!roomData.isEmpty()) {
            for (String[] room : roomData) {
                String roomStream = Stream.of(room).collect(Collectors.joining(COMMA_DELIMITER));
                buffer.append(roomStream + '\n');
            }
        }

        buffer.append(Stream.of(last).collect(Collectors.joining(COMMA_DELIMITER)));

        return new BufferedReader(new StringReader(buffer.toString()));
    }

    /**
     * Helper method that saves Rooms from Array List into the data base
     *
     * @param roomData Array list containing room name, building name and room size for each room that should be created.
     */
    public void saveRooms(List<String[]> roomData, RoomService roomService) throws NumberFormatException {
        for (String[] room : roomData) {
            roomService.saveRoom(new Room(
                    room[1],                    // room name
                    room[0],                    // building name
                    Integer.parseInt(room[2])   // parse room size from string to int
            ));
        }
    }

    /**
     * builds Array List with fixed buildingName and roomSize from String Array containing room names
     *
     * @param roomNames Array containing room names
     * @return Array List where every Array contains data to create and save a Room (room name, building name and room size).
     */
    public List<String[]> createRoomData(String[] roomNames) {
        List<String[]> roomData = new ArrayList<>();
        String buildingName = "A";
        String size = "3";

        for (String name : roomNames) {
            roomData.add(new String[]{
                    buildingName,
                    name,
                    size
            });
        }

        return roomData;
    }
}
