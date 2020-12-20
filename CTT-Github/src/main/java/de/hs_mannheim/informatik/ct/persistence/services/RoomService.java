package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.util.Optional;

@Service
public class RoomService {
    private final String COMMA_DELIMITER = ";";

    @Autowired
    private RoomRepository roomsRepo;

    public Optional<Room> findByName(String roomName) {
        return roomsRepo.findById(roomName);
    }

    public Room saveRoom(@NonNull Room room) {
        return roomsRepo.save(room);
    }

    public void ImportFromCSV(BufferedReader csv) {
        csv.lines()
                .map((line) -> {
                   String[] values = line.split(COMMA_DELIMITER);
                   String roomName = values[0];
                   int roomCapacity = Integer.parseInt(values[1]);
                   return new Room(roomName, roomCapacity);
                })
                .forEach(this::saveRoom);
    }
}
