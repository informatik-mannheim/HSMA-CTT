package de.hs_mannheim.informatik.ct.end_to_end;

import com.sun.istack.NotNull;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// todo split into:                     todo:
//  - RoomServiceTest                   mock
//  - ImportRoomsFromFile/RoomPinTests  comment
//  - RoomServiceHelper

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

    private final String COMMA_DELIMITER = ";";

    // CSV tests
    @Test
    public void importCsv() throws IOException {
        String roomName = "A001a";
        List<String[]> testRoomData = createRoomData(new String[]{roomName});

        BufferedReader csvReader = createCsvReader(testRoomData);
        roomService.importFromCsv(csvReader);

        Optional<Room> testRoom = roomService.findByName(roomName);

        assertThat(testRoom.isPresent(), is(true));
    }

    @Test
    public void importCsv_EmptyFile() {
        String exceptedMessages = "CSV of room import not correct formatted";
        BufferedReader csvReader = new BufferedReader(new StringReader(";;"));
        Exception exception = assertThrows(Exception.class, () -> {
            roomService.importFromCsv(csvReader);
        });

        assertThat(exception.getMessage(), equalTo(exceptedMessages));
    }

    @Test
    public void importCsv_wrongCommaDelimiter() {
        String exceptedMessages = "CSV of room import not correct formatted";
        BufferedReader csvReaderSingleLine = new BufferedReader(
                new StringReader("room,building,2"));

        BufferedReader csvReaderTwoLines = new BufferedReader(
                new StringReader("room;building;1\n" +
                                    "room2;building,2"));

        Exception exceptionSingleLine = assertThrows(Exception.class, () -> {
            roomService.importFromCsv(csvReaderSingleLine);
        });
        Exception exceptionTwoLines = assertThrows(Exception.class, () -> {
            roomService.importFromCsv(csvReaderTwoLines);
        });

        assertThat(exceptionSingleLine.getMessage(), equalTo(exceptedMessages));
        assertThat(exceptionTwoLines.getMessage(), equalTo(exceptedMessages));
    }

    @Test
    public void importCsv_wrongRoomCapacityType() {
        String exceptedMessages = "CSV of room import not correct formatted";
        BufferedReader csvReader = new BufferedReader(
                new StringReader("room;building;zwölf"));

        Exception exception = assertThrows(Exception.class, () -> {
            roomService.importFromCsv(csvReader);
        });

        assertThat(exception.getMessage(), equalTo(exceptedMessages));
    }

    @Test
    public void importCsv_overrideSingleRoom() throws IOException {
        String roomName = "newTestRoom";
        List<String[]> testRoomData = createRoomData(new String[]{roomName});

        saveRooms(testRoomData);

        String initialTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        BufferedReader csvReader = createCsvReader(testRoomData);
        roomService.importFromCsv(csvReader);

        String newTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        assertThat(newTestRoomPin, equalTo(initialTestRoomPin));
    }

    @Test
    public void importCsv_overrideMultipleRooms() throws IOException {
        String[] roomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = createRoomData(roomNames);

        saveRooms(testRoomData);

        String[] initialTestRoomPins = extractRoomPins(roomNames);

        BufferedReader csvData = createCsvReader(testRoomData);

        roomService.importFromCsv(csvData);

        //roomService.importFromCsv(csvReader);

        String[] newTestRoomPins = extractRoomPins(roomNames);

        for (int i = 0; i < initialTestRoomPins.length; i++) {
            assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
        }
    }

    @Test
    public void importCsv_overrideExistingAndNewRooms() throws IOException {
        String[] existingRoomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = createRoomData(existingRoomNames);

        saveRooms(testRoomData);

        String[] initialTestRoomPins = extractRoomPins(existingRoomNames);

        String[] newRoomNames = new String[]{"TestR00m", "otherTestRoom", "test2", "room"};
        List<String[]> testRoomData2 = createRoomData(newRoomNames);
        BufferedReader csvData = createCsvReader(testRoomData2);

        roomService.importFromCsv(csvData);

        String[] newTestRoomPins = extractRoomPins(newRoomNames);

        for (int i = 0; i < initialTestRoomPins.length; i++) {
            if (existingRoomNames[i].equals(newRoomNames[i])) {
                assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
            }
        }
    }

    // Excel tests
    // Todo methods to implement. Find a way to customize excel file
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

    // todo need rework. This method contains an ugly workaround with hardcoded values.
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
     * Helper Method to load excel file
     * @param testExcelPath Filename of excel file. Needs to be stored in resources folder
     * @return InputStream containing data as if it was loaded from an existing excel file
     */
    private InputStream inputStreamFromTestExcel(String testExcelPath) throws IOException {
        File excelFile = new ClassPathResource(testExcelPath).getFile();
        InputStream is = new FileInputStream(excelFile);
        return is;
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

    /**
     * Helper Method to create Buffered Reader from Array List. This is used to test a room import
     * feature without generating and reading from a csv file.
     *
     * @aparam roomData Arraylist holding room data.
     */
    private BufferedReader createCsvReader(@NotNull List<String[]> roomData) {
        StringBuilder buffer = new StringBuilder();

        for (String[] room : roomData) {
            buffer.append(Stream.of(room).collect(Collectors.joining(COMMA_DELIMITER)));
            buffer.append('\n');
        }

        return new BufferedReader(new StringReader(buffer.toString()));
    }

    /**
     * Helper method that saves Rooms from Array List into the data base
     *
     * @param roomData Array list containing room name, building name and room size for each room that should be created.
     */
    private void saveRooms(List<String[]> roomData) throws NumberFormatException {
        for (String[] room : roomData) {
            roomService.saveRoom(new Room(
                    room[0],                    // room name
                    room[1],                    // building name
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
    private List<String[]> createRoomData(String[] roomNames) {
        List<String[]> roomData = new ArrayList<>();
        String buildingName = "A";
        String size = "3";

        for (String name : roomNames) {
            roomData.add(new String[]{
                    name,
                    buildingName,
                    size
            });
        }

        return roomData;
    }
}

