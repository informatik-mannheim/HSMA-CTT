/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.hs_mannheim.informatik.ct.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import de.hs_mannheim.informatik.ct.model.Room;

public interface RoomRepository extends JpaRepository<Room, String> {

    // TODO: is this optimal and smart to do it like this?
    @Modifying
    @Transactional
    @Query(value = "BACKUP TO ?1", nativeQuery = true)
    int backupH2DB(String path);

    @Query("SELECT DISTINCT room.buildingName " +
            "FROM Room room " +
            "ORDER BY room.buildingName")
    List<String> getAllBuildings();

    Optional<Room> findByNameIgnoreCase(String roomName);

    List<Room> findByBuildingName(String building);

    @Query("SELECT COALESCE(SUM(room.maxCapacity), 0) " +
            "FROM Room room " +
            "WHERE room.name in :studyRooms")
    int getTotalStudyRoomsCapacity(@Param("studyRooms") String[] studyRooms);
}
