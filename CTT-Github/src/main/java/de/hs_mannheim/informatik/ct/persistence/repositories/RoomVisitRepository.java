package de.hs_mannheim.informatik.ct.persistence.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.RoomVisitContact;

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
	 * @return All visitors that haven't checked out yet.
	 */
	@Query("SELECT visit " +
			"FROM RoomVisit visit " +
			"WHERE visit.endDate is null")
	List<RoomVisit> findNotCheckedOutVisits();

	void deleteByEndDateBefore(Date endDate);

	@Query("SELECT NEW de.hs_mannheim.informatik.ct.model.RoomVisitContact(visitTarget, visitOther) " +
			"FROM RoomVisit visitTarget," +
			"RoomVisit visitOther " +
			"WHERE visitTarget.visitor = :visitor AND " +
			"visitTarget.room = visitOther.room AND " +
			"visitTarget.startDate <= visitOther.endDate AND " +
			"visitOther.startDate <= visitTarget.endDate " +
			"ORDER BY visitTarget.start")
	List<RoomVisitContact> findVisitsWithContact(@Param(value = "visitor") Visitor visitor);
	
}
