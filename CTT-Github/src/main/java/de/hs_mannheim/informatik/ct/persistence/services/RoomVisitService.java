package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomVisitService {

    @Autowired
    private RoomVisitRepository roomVisitRepository;

    public RoomVisit visitRoom(Besucher visitor, Room room) {
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
    public List<RoomVisit> checkOutVisitor(@NonNull Besucher visitor) {
        List<RoomVisit> notSignedOutVisits = getCheckedInRoomVisits(visitor);
        notSignedOutVisits.forEach((visit) -> {
            visit.setEnd(new Date());
            roomVisitRepository.save(visit);
        });

        return notSignedOutVisits;
    }

    @NonNull
    public List<RoomVisit> getCheckedInRoomVisits(@NonNull Besucher visitor) {
        return roomVisitRepository.findNotCheckedOutVisits(visitor);
    }

    public void checkOutAllVisitors(@NonNull LocalTime forcedVisitEndTime) {
        // Setting the end time in Java makes it easier to set it correctly.
        // Concretely, visits will always end on the same day even if processing was delayed.
        val notCheckedOut = roomVisitRepository.findNotCheckedOutVisits();
        val updatedVisits = notCheckedOut
                .stream()
                .peek((roomVisit -> {
                    val startTime = convertToLocalTime(roomVisit.getStart());
                    val startDate = convertToLocalDate(roomVisit.getStart());

                    if(startTime.isBefore(forcedVisitEndTime)) {
                        val endDate = startDate.atTime(forcedVisitEndTime);
                        roomVisit.setEnd(convertToDate(endDate));
                    } else if(startDate.isBefore(LocalDate.now())){
                        // Visit started yesterday after forced sign-out time, sign-out at midnight yesterday instead
                        val endDate = startDate.atTime(LocalTime.parse("23:59:59"));
                        roomVisit.setEnd(convertToDate(endDate));
                    }
                }))
                .collect(Collectors.toList());

        roomVisitRepository.saveAll(updatedVisits);
    }

    public int getVisitorCount(@NonNull Room room) {
        return roomVisitRepository.getRoomVisitorCount(room);
    }

    public boolean isRoomFull(@NonNull Room room) {
        return getVisitorCount(room) >= room.getMaxCapacity();
    }

    public void deleteExpiredRecords(Duration recordLifeTime) {
        val oldestAllowedRecord = LocalDateTime.now().minus(recordLifeTime);
        roomVisitRepository.deleteByEndBefore(convertToDate(oldestAllowedRecord));
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private LocalTime convertToLocalTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
    }

    private Date convertToDate(LocalDateTime dateTime) {
        return java.sql.Timestamp.valueOf(dateTime);
    }
}
