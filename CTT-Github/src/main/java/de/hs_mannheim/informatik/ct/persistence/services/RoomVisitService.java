package de.hs_mannheim.informatik.ct.persistence.services;

import static de.hs_mannheim.informatik.ct.util.TimeUtil.convertToDate;
import static de.hs_mannheim.informatik.ct.util.TimeUtil.convertToLocalDate;
import static de.hs_mannheim.informatik.ct.util.TimeUtil.convertToLocalTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.RoomVisitContact;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import lombok.NonNull;
import lombok.val;

@Service
public class RoomVisitService {

	@Autowired
	private RoomVisitRepository roomVisitRepository;

	public RoomVisit visitRoom(Visitor visitor, Room room) {
		return roomVisitRepository.save(new RoomVisit(visitor, room, new Date()));
	}

	/**
	 * Checks the visitor out from all currently checked-in visits and returns the visits.
	 * Their should usually be only one checked in visit at a time.
	 *
	 * @param visitor The visitor who is checked out of their visits.
	 * @return The rooms the visitor was checked into.
	 */
	@NonNull
	public List<RoomVisit> checkOutVisitor(@NonNull Visitor visitor) {
		List<RoomVisit> notSignedOutVisits = getCheckedInRoomVisits(visitor);
		notSignedOutVisits.forEach((visit) -> {
			visit.setEndDate(new Date());
			roomVisitRepository.save(visit);
		});

		return notSignedOutVisits;
	}

	@NonNull
	public List<RoomVisit> getCheckedInRoomVisits(@NonNull Visitor visitor) {
		return roomVisitRepository.findNotCheckedOutVisits(visitor);
	}

	public void checkOutAllVisitors(@NonNull LocalTime forcedVisitEndTime) {
		// Setting the end time in Java makes it easier to set it correctly.
		// Concretely, visits will always end on the same day even if processing was delayed.
		val notCheckedOut = roomVisitRepository.findNotCheckedOutVisits();
		val updatedVisits = notCheckedOut
				.stream()
				.map((roomVisit -> {
					val startTime = convertToLocalTime(roomVisit.getStartDate());
					val startDate = convertToLocalDate(roomVisit.getStartDate());

					if (startTime.isBefore(forcedVisitEndTime)) {
						val endDate = startDate.atTime(forcedVisitEndTime);
						roomVisit.setEndDate(convertToDate(endDate));
					} else if (startDate.isBefore(LocalDate.now())) {
						// Visit started yesterday after forced sign-out time, sign-out at midnight yesterday instead
						val endDate = startDate.atTime(LocalTime.parse("23:59:59"));
						roomVisit.setEndDate(convertToDate(endDate));
					} else {
						return null;
					}

					return roomVisit;
				}))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		roomVisitRepository.saveAll(updatedVisits);
	}

	public int getVisitorCount(@NonNull Room room) {
		return roomVisitRepository.getRoomVisitorCount(room);
	}

	public boolean isRoomFull(@NonNull Room room) {
		return getVisitorCount(room) >= room.getMaxCapacity();
	}

	public void deleteExpiredRecords(Period recordLifeTime) {
		val oldestAllowedRecord = LocalDateTime.now().minus(recordLifeTime);
		roomVisitRepository.deleteByEndDateBefore(convertToDate(oldestAllowedRecord));
	}

	public List<RoomVisitContact> getVisitorContacts(@NonNull Visitor visitor) {
		return roomVisitRepository.findVisitsWithContact(visitor);
	}

}
