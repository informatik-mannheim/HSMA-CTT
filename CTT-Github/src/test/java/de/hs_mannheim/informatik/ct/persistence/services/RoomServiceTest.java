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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Test
    public void importFromCsvResetsRoomPins() throws Exception {
        // create testroom and csv
        Room testRoom = roomService.saveRoom(new Room("testRoom", "A", 10));
        createTestCsv("testRoom", "A", 10);

        String initialTestRoomPin = testRoom.getRoomPin();

        // import and delete csv
        roomService.importFromCsv(new BufferedReader(new FileReader(TEST_CSV_FILENAME)));
        String newTestRoomPin = roomService.findByName("testRoom").get().getRoomPin();

        Assertions.assertEquals(newTestRoomPin, initialTestRoomPin);
    }

    public void createTestCsv(String roomName, String buildingName, int maxCapacity) throws IOException {
        File csvOutputFile = new File(TEST_CSV_FILENAME);

        //todo check if file TEST_CSV_FILENAME already exists to be sure nothing gets overwritten
        String[] data = new String[]{ buildingName, roomName, String.valueOf(maxCapacity)};

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.format(Stream.of(data).collect(Collectors.joining(COMMA_DELIMITER)));
        }

        csvOutputFile.deleteOnExit();
    }
}
