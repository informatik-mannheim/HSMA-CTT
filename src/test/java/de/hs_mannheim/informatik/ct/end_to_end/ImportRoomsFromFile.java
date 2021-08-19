package de.hs_mannheim.informatik.ct.end_to_end;

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

import de.hs_mannheim.informatik.ct.RoomServiceHelper;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.util.RoomTypeConverter;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ImportRoomsFromFile {
    @TestConfiguration
    static class RoomControllerTestConfig {
        @Bean
        public RoomService service() {
            return new RoomService();
        }
    }

    @Autowired
    private RoomService roomService;

    private RoomServiceHelper helper = new RoomServiceHelper();

    // these tests verify that room pins won't change after usage of import functions provided by RoomService.java

    // CSV tests
    // one room in csv, same room in DB
    @Test
    public void importCsv_overrideSingleRoom_EmptyDB() throws IOException {
        String roomName = "newTestRoom";
        List<String[]> testRoomData = helper.createRoomData(new String[]{roomName});

        helper.saveRooms(testRoomData, roomService);

        String initialTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        BufferedReader csvReader = helper.createCsvBuffer(testRoomData);
        roomService.importFromCsv(csvReader);

        String newTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        assertThat(newTestRoomPin, equalTo(initialTestRoomPin));
    }

    // one room in csv, multiple rooms in DB
    @Test
    public void importCsv_overrideSingleRoom_FilledDB() throws IOException {
        String[] roomNames = {"newTestRoom", "some", "other", "rooms", "roomRoom", "emptyRoom"};
        List<String[]> testRoomData = helper.createRoomData(roomNames);

        helper.saveRooms(testRoomData, roomService);

        String initialTestRoomPin[] = extractRoomPins(roomNames);

        BufferedReader csvReader = helper.createCsvBuffer(helper.createRoomData(new String[]{roomNames[0]}));
        roomService.importFromCsv(csvReader);

        String[] newTestRoomPin = extractRoomPins(roomNames);

        assertThat(newTestRoomPin, equalTo(initialTestRoomPin));
    }

    // multiple rooms in csv, same rooms in DB but no others
    @Test
    public void importCsv_overrideMultipleRooms_EmptyDB() throws IOException {
        String[] roomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = helper.createRoomData(roomNames);

        helper.saveRooms(testRoomData, roomService);

        String[] initialTestRoomPins = extractRoomPins(roomNames);

        BufferedReader csvData = helper.createCsvBuffer(testRoomData);

        roomService.importFromCsv(csvData);

        String[] newTestRoomPins = extractRoomPins(roomNames);

        assertThat(initialTestRoomPins, equalTo(newTestRoomPins));
    }

    // multiple rooms in csv, same rooms and additional rooms in DB
    @Test
    public void importCsv_overrideMultipleRooms_FilledDB() throws IOException {
        String[] roomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        String[] additionalRoomNames = new String[]{"other", "rooms", "roomRoom", "emptyRoom"};

        List<String[]> testRoomData = helper.createRoomData(roomNames);
        List<String[]> additionalData = helper.createRoomData(additionalRoomNames);

        helper.saveRooms(testRoomData, roomService);
        helper.saveRooms(additionalData, roomService);

        String[] initialTestRoomPins = extractRoomPins(roomNames);
        String[] additionalInitialRoomPins = extractRoomPins(additionalRoomNames);

        BufferedReader csvData = helper.createCsvBuffer(testRoomData);

        roomService.importFromCsv(csvData);

        String[] newTestRoomPins = extractRoomPins(roomNames);
        String[] additionalNewRoomPins = extractRoomPins(additionalRoomNames);

        // checks overwritten rooms
        assertThat(initialTestRoomPins, equalTo(newTestRoomPins));
        // checks not overwritten rooms
        assertThat(additionalInitialRoomPins, equalTo(additionalNewRoomPins));
    }

    // existing and new rooms in csv
    @Test
    public void importCsv_overrideRoomsAndAddNew_EmptyDB() throws IOException {
        String[] roomNamesDB = new String[]{"newTestRoom", "otherTestRoom"};
        String[] roomNamesImport = new String[]{roomNamesDB[0], roomNamesDB[1], "roomRoom", "emptyRoom"};

        List<String[]> dataDB = helper.createRoomData(roomNamesDB);
        List<String[]> dataImport = helper.createRoomData(roomNamesImport);

        helper.saveRooms(dataDB, roomService);

        String[] initialTestRoomPins = extractRoomPins(roomNamesDB);

        BufferedReader csvData = helper.createCsvBuffer(dataImport);

        roomService.importFromCsv(csvData);

        String[] newTestRoomPins = extractRoomPins(roomNamesDB);

        assertThat(initialTestRoomPins, equalTo(newTestRoomPins));
    }

    // existing and new rooms in csv, additional rooms that won't get overwritten in DB
    @Test
    public void importCsv_overrideRoomsAndAddNew_FilledDB() throws IOException {
        String[] existingRoomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = helper.createRoomData(existingRoomNames);

        helper.saveRooms(testRoomData, roomService);

        String[] initialTestRoomPins = extractRoomPins(existingRoomNames);

        String[] newRoomNames = new String[]{"TestR00m", "otherTestRoom", "test2", "room"};
        List<String[]> testRoomData2 = helper.createRoomData(newRoomNames);
        BufferedReader csvData = helper.createCsvBuffer(testRoomData2);

        roomService.importFromCsv(csvData);

        String[] newTestRoomPins = extractRoomPins(newRoomNames);

        for (int i = 0; i < initialTestRoomPins.length; i++) {
            if (existingRoomNames[i].equals(newRoomNames[i])) {
                assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
            }
        }
    }

    // check room type after import
    @Test
    public void importCsv_checkRoomTypeAfterImport() throws IOException {
        String[] newRoomNames = new String[]{"TestR00m", "otherTestRoom", "test2", "room"};
        List<String[]> testRoomData = helper.createRoomData(newRoomNames);

        ArrayList<String> roomRNAs = new ArrayList<>();
        for (String[] roomData : testRoomData) {
            roomRNAs.add(roomData[3]);
        }

        roomService.importFromCsv(helper.createCsvBuffer(testRoomData));

        for(String[] roomData : testRoomData){
            Optional<Room> room = roomService.findByName(roomData[1]);
            assertThat(room.isPresent(), equalTo(true));
            if(room.isPresent()){
                assertThat(room.get().getType(), equalTo(RoomTypeConverter.RoomType.HOERSAAL));
            }
        }
    }

    @Test
    public void importExcel_overriteMultipleRooms() throws Exception {
        String testExcelPath = "excelForTest.xlsm";
        // needs to be changed if the values in the testfile change.
        String[] roomNames = new String[]{"A-101", "A-102"};
        List<String[]> testRoomData = helper.createRoomData(roomNames);

        helper.saveRooms(testRoomData, roomService);

        String[] initialTestRoomPins = extractRoomPins(roomNames);

        roomService.saveRoom(
                new Room("A-101", "A", 2)
        );
        roomService.saveRoom(
                new Room("A-102", "A", 8)
        );

        InputStream is = helper.inputStreamFromTestExcel(testExcelPath);
        roomService.importFromExcel(is);

        String[] newTestRoomPins = extractRoomPins(roomNames);

        for (int i = 0; i < initialTestRoomPins.length; i++) {
            assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
        }
    }

    /**
     * Helper Method to extract room pins
     *
     * @param roomNames string array holding the room names
     * @return array holding the room pins
     */

    private String[] extractRoomPins(String[] roomNames) throws NoSuchElementException {
        String[] roomPins = new String[roomNames.length];
        for (int i = 0; i < roomNames.length; i++) {
            Optional<Room> room = roomService.findByName(roomNames[i]);
            if (room.isPresent()) {
                roomPins[i] = room.get().getRoomPin();
            } else {
                String errorMessage = "Keine Raum-Pin vorhanden. Raum " + roomNames[i] + " konnte nicht gefunden werden.";
                throw new NoSuchElementException(errorMessage);
            }
        }
        return roomPins;
    }

}

