package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Room;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;


/* todo
    create csv from arraylist       refactor and comment
    outsource roompin extraction
    find a way to delete csv file
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class RoomServiceTest {
    @TestConfiguration
    static class RoomControllerTestConfig {
        @Bean
        public RoomService service() {
            return new RoomService();
        }
    }

    @Autowired
    private RoomService roomService;

    // filename including file extension (e.g. testFile.csv)
    private final String TEST_CSV_FILENAME = "testFile.csv";
    private final String COMMA_DELIMITER = ";";

    // existing room in csv
    @Test
    public void importCsvSingleRoom() throws Exception {
        // todo create array list, saveRooms(), createCsv() and assert
        String roomName = "newTestRoom";
        List<String[]> testRoomData = createRoomData(new String[]{roomName});

        saveRooms(testRoomData);

        String initialTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        createTestCsv(testRoomData);

        roomService.importFromCsv(new BufferedReader(new FileReader(TEST_CSV_FILENAME)));

        String newTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        assertThat(newTestRoomPin, equalTo(initialTestRoomPin));

        deleteTestCsv();
    }

    // new and existing rooms in csv
    @Test
    public void importCsvMultipleRooms() throws IOException {
        String[] roomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = createRoomData(roomNames);

        saveRooms(testRoomData);

        String[] initialTestRoomPins = new String[roomNames.length];
        for(int i = 0; i < roomNames.length; i++){
            initialTestRoomPins[i] = roomService.findByName(roomNames[i]).get().getRoomPin();
        }

        createTestCsv(testRoomData);

        roomService.importFromCsv(new BufferedReader(new FileReader(TEST_CSV_FILENAME)));

        String[] newTestRoomPins = new String[roomNames.length];
        for(int i = 0; i < roomNames.length; i++){
            newTestRoomPins[i] = roomService.findByName(roomNames[i]).get().getRoomPin();
        }

        for(int i = 0; i < initialTestRoomPins.length; i++){
            assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
        }

        deleteTestCsv();
    }

    public void createTestCsv(List<String[]> roomData) throws IOException {
        File csvFile = new File(TEST_CSV_FILENAME);

        if(csvFile.exists()){
            throw new FileAlreadyExistsException("%s already exists. Remove this file or change TEST_CSV_FILENAME");
        }

        try (PrintWriter pw = new PrintWriter(csvFile)) {
           //pw.format(Stream.of(data).collect(Collectors.joining(COMMA_DELIMITER)));
            roomData.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .collect(Collectors.joining(COMMA_DELIMITER));
    }

    public void deleteTestCsv(){
        File csvFile = new File(TEST_CSV_FILENAME);
        if(csvFile.exists()){
            csvFile.delete();
            System.out.println("FILE HAS BEEN REMOVED!");
        }
    }

    /**
     *  Helper method that saves Rooms from Array List into the data base
     * @param roomData Array list containing room name, building name and room size for each room that should be created.
     */
    public void saveRooms(List<String[]> roomData) throws NumberFormatException{
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
     * @param roomNames Array containing room names
     * @return Array List where every Array contains data to create and save a Room (room name, building name and room size).
     */
    public List<String[]> createRoomData(String[] roomNames){
        List<String[]> roomData = new ArrayList<>();
        String buildingName = "A";
        String size = "3";

        for (String name : roomNames){
            roomData.add(new String[]{
                    name,
                    buildingName,
                    size
            });
        }

        return roomData;
    }
}
