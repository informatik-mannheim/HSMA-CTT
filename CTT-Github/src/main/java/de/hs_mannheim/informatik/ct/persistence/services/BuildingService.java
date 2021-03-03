package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {
    @Autowired
    private RoomRepository roomRepository;

    public List<String> getAllBuildings() {
        return roomRepository.getAllBuildings();
    }

    public List<Room> getAllRoomsInBuilding(String building) {
        return roomRepository.findByBuildingName(building);
    }
}
