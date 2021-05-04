package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.RoomServiceHelper;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

//todo mock
@ExtendWith(SpringExtension.class)
@DataJpaTest
public class RoomSerivceTest {
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

    @Test
    public void importCsv() throws IOException {
        String roomName = "A001a";
        List<String[]> testRoomData = helper.createRoomData(new String[]{roomName});

        BufferedReader csvReader = helper.createCsvBuffer(testRoomData);
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
}
