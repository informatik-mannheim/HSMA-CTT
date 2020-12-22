package de.hs_mannheim.informatik.ct.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import de.hs_mannheim.informatik.ct.model.Room;

public interface RoomRepository extends JpaRepository<Room, String> {
}
