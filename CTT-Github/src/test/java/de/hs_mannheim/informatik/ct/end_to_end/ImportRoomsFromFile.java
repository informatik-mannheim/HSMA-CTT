package de.hs_mannheim.informatik.ct.end_to_end;

import com.sun.istack.NotNull;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import java.io.*;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final String COMMA_DELIMITER = ";";

    // overwrite existing room with csv import
    @Test
    public void importCsvSingleRoom() throws Exception {
        String roomName = "newTestRoom";
        List<String[]> testRoomData = createRoomData(new String[]{roomName});

        saveRooms(testRoomData);

        String initialTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        roomService.importFromCsv(createReader(testRoomData));

        String newTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        assertThat(newTestRoomPin, equalTo(initialTestRoomPin));
    }


    // new and existing rooms in csv
    @Test
    public void importCsvMultipleRooms() throws IOException {
        String[] roomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = createRoomData(roomNames);

        saveRooms(testRoomData);

        String[] initialTestRoomPins = extractRoomPins(roomNames);

        roomService.importFromCsv(createReader(testRoomData));

        String[] newTestRoomPins = extractRoomPins(roomNames);

        for (int i = 0; i < initialTestRoomPins.length; i++) {
            assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
        }
    }

    // new and existing rooms
    @Test
    public void importCsvExistingNonExistingRooms()  {
        String[] existingRoomNames = new String[]{"newTestRoom", "otherTestRoom", "test", "room"};
        List<String[]> testRoomData = createRoomData(existingRoomNames);
        saveRooms(testRoomData);
        String[] initialTestRoomPins = extractRoomPins(existingRoomNames);

        String[] newRoomNames = new String[]{"newTestRoom2", "otherTestRoom", "test2", "room"};
        List<String[]> testRoomData2 = createRoomData(newRoomNames);

        roomService.importFromCsv(createReader(testRoomData2));
        String[] newTestRoomPins = extractRoomPins(newRoomNames);

        for (int i = 0; i < initialTestRoomPins.length; i++) {
            if (existingRoomNames[i].equals(newRoomNames[i])) {
                assertThat(initialTestRoomPins[i], equalTo(newTestRoomPins[i]));
            } else {
                assertThat(initialTestRoomPins[i], not(equalTo(newTestRoomPins[i])));
            }
        }
    }

    /*
    // overwrite existing room with excel
    @Test
    public void importExcelSingleRoom() throws Exception {
        String roomName = "newTestRoom";
        List<String[]> testRoomData = createRoomData(new String[]{roomName});

        saveRooms(testRoomData);

        String initialTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        roomService.importFromExcel(createReaderExcel(testRoomData));

        String newTestRoomPin = roomService.findByName(roomName).get().getRoomPin();

        assertThat(newTestRoomPin, equalTo(initialTestRoomPin));
    }

    private InputStream createReaderExcel(@NotNull List<String[]> roomData) {
        StringBuilder buffer = new StringBuilder();
        for (String[] room : roomData) {
            buffer.append(Stream.of(room).collect(Collectors.joining(COMMA_DELIMITER)));
            buffer.append('\n');
        }
        return new ByteArrayInputStream(Charset.forName("UTF-16").encode(buffer.toString()).array());
    }*/
    

    /**
     * Helper Method to extract room pins
     *
     * @param roomNames string array holding the room names
     * @return array holding the room pins
     */
    private String[] extractRoomPins(String[] roomNames) {
        String[] roomPins = new String[roomNames.length];
        for (int i = 0; i < roomNames.length; i++) {
            roomPins[i] = roomService.findByName(roomNames[i]).get().getRoomPin();
        }
        return roomPins;
    }

    /**
     * Helper Method to create Buffered Reader from Array List. This is used to test a room import
     * feature without generating and reading from a csv file.
     *
     * @aparam roomData Arraylist holding room data.
     */
    private BufferedReader createReader(@NotNull List<String[]> roomData) {
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

