package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.util.Optional;
import java.util.List;

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

    @NonNull
    public List<Room> all() {
        return roomsRepo.findAll();
    }

    //TODO: Maybe check if csv is correctly formatted (or accept that the user uploads only correct files?)
    public void importFromCsv(BufferedReader csv) {
        csv.lines()
                .map((line) -> {
                    String[] values = line.split(COMMA_DELIMITER);
                    String building = values[0];
                    String roomName = values[1];
                    int roomCapacity = Integer.parseInt(values[2]);
                    return new Room(roomName, building, roomCapacity);
                })
                .forEach(this::saveRoom);
    }
}