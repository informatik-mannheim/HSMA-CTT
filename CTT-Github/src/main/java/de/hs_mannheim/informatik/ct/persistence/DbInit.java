package de.hs_mannheim.informatik.ct.persistence;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class DbInit implements CommandLineRunner {
    @Autowired
    private RoomRepository roomsRepo;

    // TODO: nur testweise für den Moment, später wieder entfernen
    @Override
    public void run(String... args) {
        roomsRepo.save(new Room("A007a", 3));
        roomsRepo.save(new Room("test", 12));
        roomsRepo.save(new Room("A210", 19));
    }

}
