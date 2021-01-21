package de.hs_mannheim.informatik.ct.persistence.repositories;


import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


public interface RoomVisitRepository extends JpaRepository<RoomVisit, Long> {
    @Query("SELECT visit " +
            "FROM RoomVisit visit " +
            "WHERE visit.visitor = :visitor and visit.end is null")
    List<RoomVisit> findNotCheckedOutVisits(@Param(value = "visitor") Besucher visitor);

    @Query("SELECT COUNT (visit) " +
            "FROM RoomVisit visit " +
            "WHERE visit.room = :room and visit.end is null ")
    int getRoomVisitorCount(@Param(value = "room") Room room);

    /**
     * Finds all visitors that haven't checked out yet.
     * @return All visitors that haven't checked out yet.
     */
    @Query("SELECT visit " +
            "FROM RoomVisit visit " +
            "WHERE visit.end is null")
    List<RoomVisit> findNotCheckedOutVisits();

    void deleteByEndBefore(Date end);
}
