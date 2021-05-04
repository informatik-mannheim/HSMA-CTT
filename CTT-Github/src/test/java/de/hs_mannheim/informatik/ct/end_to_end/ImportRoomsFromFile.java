package de.hs_mannheim.informatik.ct.end_to_end;

import de.hs_mannheim.informatik.ct.RoomServiceHelper;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import java.io.*;
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

    // CSV tests
    @Test
    public void importCsv_overrideSingleRoom() throws IOException {
        String roomName = "newTestRoom";
        List<String[]> testRoomData = helper.createRoomData(new String[]{roomName});

        helper.saveRooms(testRoomData, roomService);

        String initialTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        BufferedReader csvReader = helper.createCsvBuffer(testRoomData);
        roomService.importFromCsv(csvReader);

        String newTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        assertThat(newTestRoomPin, equalTo(initialTestRoomPin));
    }

    @Test
    public void importCsv_overrideMultipleRooms() throws IOException {
        String[] roomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = helper.createRoomData(roomNames);

        helper.saveRooms(testRoomData, roomService);

        String[] initialTestRoomPins = extractRoomPins(roomNames);

        BufferedReader csvData = helper.createCsvBuffer(testRoomData);

        roomService.importFromCsv(csvData);

        String[] newTestRoomPins = extractRoomPins(roomNames);

        for (int i = 0; i < initialTestRoomPins.length; i++) {
            assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
        }
    }

    @Test
    public void importCsv_overrideExistingAndNewRooms() throws IOException {
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
/**
    // Excel tests
    @Test
    public void importExcel() throws IOException{
        String testExcelPath = "excelForTest.xlsm";
        InputStream is = inputStreamFromTestExcel(testExcelPath);

        roomService.importFromExcel(is);
    }

    @Test
    public void importExcel_EmptyFile() throws IOException {
        String testExcelPath = "excelForTest-empty.xlsm";
        InputStream is = inputStreamFromTestExcel(testExcelPath);

        assertThrows(Exception.class, () -> {
            roomService.importFromExcel(is);
        });
    }

    @Test
    public void importExcel_WrongRoomCapacityType() throws IOException {
        String testExcelPath = "excelForTest-wrongRoomCapacityType.xlsm";
        InputStream is = inputStreamFromTestExcel(testExcelPath);

        assertThrows(Exception.class, () -> {
            roomService.importFromExcel(is);
        });
    }

    @Test
    public void importExcel_overriteMultipleRooms() throws Exception {
        String testExcelPath = "excelForTest.xlsm";
        // needs to be changed if the values in the testfile change.
        String[] roomNames = new String[]{"A-101", "A-102"};
        List<String[]> testRoomData = createRoomData(roomNames);
        String[] initialTestRoomPins = extractRoomPins(roomNames);

        roomService.saveRoom(
                new Room("A-101", "A", 2)
        );
        roomService.saveRoom(
                new Room("A-102", "A", 8)
        );
        InputStream is = inputStreamFromTestExcel(testExcelPath);
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
            if(room.isPresent()){
                roomPins[i] = room.get().getRoomPin();
            }else{
                String errorMessage = "Keine Raum-Pin vorhanden. Raum " + roomNames[i] + " konnte nicht gefunden werden.";
                throw new NoSuchElementException(errorMessage);
            }
        }
        return roomPins;
    }

}

