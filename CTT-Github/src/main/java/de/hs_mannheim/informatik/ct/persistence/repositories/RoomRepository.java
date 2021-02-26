package de.hs_mannheim.informatik.ct.persistence.repositories;

import de.hs_mannheim.informatik.ct.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> {

    // TODO: is this optimal and smart to do it like this?
    @Modifying
    @Transactional
    @Query(value = "BACKUP TO ?1", nativeQuery = true)
    int backupH2DB(String path);

    @Query("SELECT DISTINCT room.buildingName " +
            "FROM Room room")
    List<String> getAllBuildings();

    List<Room> findByBuildingName(String building);
}
