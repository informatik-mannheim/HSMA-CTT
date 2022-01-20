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

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.StudyRoom;
import de.hs_mannheim.informatik.ct.model.Visitor;
import lombok.NonNull;

public interface RoomVisitRepository extends JpaRepository<RoomVisit, Long> {
    @Query("SELECT visit " +
            "FROM RoomVisit visit " +
            "WHERE visit.visitor = :visitor and visit.endDate is null")
    List<RoomVisit> findNotCheckedOutVisits(@Param(value = "visitor") Visitor visitor);

    @Query("SELECT COUNT (visit) " +
            "FROM RoomVisit visit " +
            "WHERE visit.room = :room and visit.endDate is null ")
    int getRoomVisitorCount(@Param(value = "room") Room room);

    /**
     * Finds all visitors that haven't checked out yet.
     *
     * @return All visitors that haven't checked out yet.
     */
    @Query("SELECT visit " +
            "FROM RoomVisit visit " +
            "WHERE visit.endDate is null")
    List<RoomVisit> findNotCheckedOutVisits();

    @Query("SELECT visit " +
            "FROM RoomVisit visit " +
            "WHERE visit.room = :room AND visit.endDate is null")
    List<RoomVisit> findNotCheckedOutVisits(@NonNull Room room);

    void deleteByEndDateBefore(Date endDate);

    @Query("SELECT NEW de.hs_mannheim.informatik.ct.model.Contact(visitTarget, visitOther) " +
            "FROM RoomVisit visitTarget," +
            "RoomVisit visitOther " +
            "WHERE visitTarget.visitor = :visitor AND " +
            "visitTarget.visitor != visitOther.visitor AND " +
            "visitTarget.room = visitOther.room AND " +
            "visitTarget.startDate <= visitOther.endDate AND " +
            "visitOther.startDate <= visitTarget.endDate " +
            "ORDER BY visitTarget.startDate")
    List<Contact<RoomVisit>> findVisitsWithContact(@Param(value = "visitor") Visitor visitor);

    @Query("SELECT NEW de.hs_mannheim.informatik.ct.model.Contact(visitTarget, visitOther) " +
            "FROM RoomVisit visitTarget," +
            "RoomVisit visitOther " +
            "WHERE visitTarget.visitor = :visitor AND " +
            "visitTarget.visitor != visitOther.visitor AND " +
            "visitTarget.room = visitOther.room AND " +
            "visitTarget.startDate <= visitOther.endDate AND " +
            "visitOther.startDate <= visitTarget.endDate AND " +
            "visitOther.startDate >= :startDate " +
            "ORDER BY visitTarget.startDate")
    List<Contact<RoomVisit>> findVisitsWithContactAndStartDate(@Param(value = "visitor") Visitor visitor, @Param(value = "startDate") Date startDate);

    /**
     * Gets the total number of people currently checked in in a study room
     *
     * @param studyRooms room names of all study rooms
     * @return total number of people currently checked in in a study room
     */
    @Query("SELECT COUNT (*) " +
            "FROM RoomVisit rv " +
            "WHERE rv.room.name IN :studyRooms " +
            "AND rv.endDate is null")
    int getTotalStudyRoomsVisitorCount(@Param("studyRooms") String[] studyRooms);

    /**
     * Gets all study rooms
     *
     * @param studyRooms room names of all study rooms
     * @return List of study rooms with room infos (roomName, buildingName, maxCapacity) and current visitor count
     */
    @Query("SELECT NEW de.hs_mannheim.informatik.ct.model.StudyRoom" +
            "(r.name, r.buildingName, MAX(r.maxCapacity), COUNT (rv.room.name) AS visitorCount) " +
            "FROM Room r " +
            "LEFT JOIN RoomVisit rv ON (r.name = rv.room.name) AND rv.endDate IS null " +
            "WHERE r.name IN :studyRooms " +
            "GROUP BY r.name, r.buildingName")
    List<StudyRoom> getAllStudyRooms(@Param("studyRooms") String[] studyRooms);
}
