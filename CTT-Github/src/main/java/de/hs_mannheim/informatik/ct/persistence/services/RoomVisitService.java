package de.hs_mannheim.informatik.ct.persistence.services;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.persistence.repositories.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;



@Service
public class RoomVisitService implements VisitService<RoomVisit> {

	@Autowired
	private RoomVisitRepository roomVisitRepository;

	@Autowired
	private VisitorRepository visitorRepository;

	@Autowired
	private DateTimeService dateTimeService;

	public RoomVisit visitRoom(Visitor visitor, Room room) {
		return roomVisitRepository.save(new RoomVisit(visitor, room, dateTimeService.getDateNow()));
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
			visit.checkOut(dateTimeService.getDateNow(), RoomVisit.CheckOutSource.UserCheckout);
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

					LocalDateTime endDate;
					if (startTime.isBefore(forcedVisitEndTime)) {
						endDate = startDate.atTime(forcedVisitEndTime);
					} else if (startDate.isBefore(dateTimeService.getNow().toLocalDate())) {
						// Visit started yesterday after forced sign-out time, sign-out at midnight yesterday instead
						endDate = startDate.atTime(LocalTime.parse("23:59:59"));
					} else {
						return null;
					}

					roomVisit.checkOut(convertToDate(endDate), RoomVisit.CheckOutSource.AutomaticCheckout);

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

	/**
	 * Immediately checks out everyone in the room.
	 * @param room The room that will be cleared.
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	@Modifying
	public void resetRoom(@NonNull Room room) {
		val notCheckedOutVisits = roomVisitRepository.findNotCheckedOutVisits(room);
		for(val visit: notCheckedOutVisits) {
			visit.checkOut(dateTimeService.getDateNow(), RoomVisit.CheckOutSource.RoomReset);
		}

		roomVisitRepository.saveAll(notCheckedOutVisits);

		assert getVisitorCount(room) == 0;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void deleteExpiredRecords(Period recordLifeTime) {
		val oldestAllowedRecord = LocalDateTime.now().minus(recordLifeTime);
		roomVisitRepository.deleteByEndDateBefore(convertToDate(oldestAllowedRecord));
		visitorRepository.removeVisitorsWithNoVisits();
	}

	public List<Contact<RoomVisit>> getVisitorContacts(@NonNull Visitor visitor) {
		return roomVisitRepository.findVisitsWithContact(visitor);
	}

}
