package de.hs_mannheim.informatik.ct.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pair Class representing a target visitor having contact with another visitor
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomVisitContact {
    private RoomVisit target;
    private RoomVisit contact;
}
