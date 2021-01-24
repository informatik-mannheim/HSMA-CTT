package de.hs_mannheim.informatik.ct.persistence.services;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.model.RoomVisitContact;
import de.hs_mannheim.informatik.ct.persistence.repositories.RoomVisitRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    public int getVisitorCount(@NonNull Room room) {
        return roomVisitRepository.getRoomVisitorCount(room);
    }

    public boolean isRoomFull(@NonNull Room room) {
        return getVisitorCount(room) >= room.getMaxCapacity();
    }

    public List<RoomVisitContact> getVisitorContacts(@NonNull Besucher visitor) {
        return roomVisitRepository.findVisitsWithContact(visitor);
    }
}
