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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;

// todo verify csv file is properly created
//  delete cvs after test finished
//  comment Methods

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

        roomService.importFromCsv(new BufferedReader(new FileReader(TEST_CSV_FILENAME)));
        String newTestRoomPin = roomService.findByName("testRoom").get().getRoomPin();

        Assertions.assertEquals(newTestRoomPin, initialTestRoomPin);
    }

    public void createTestCsv(String roomName, String buildingName, int maxCapacity) throws IOException {
        // define Testdata
        List<String[]> dataLines = new ArrayList<>();
        dataLines.add(new String[]{ buildingName, roomName, String.valueOf(maxCapacity)});

        // create and write to file
        File csvOutputFile = new File(TEST_CSV_FILENAME);

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }

        assertThat(csvOutputFile, notNullValue());
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .collect(Collectors.joining(COMMA_DELIMITER));
    }
}
