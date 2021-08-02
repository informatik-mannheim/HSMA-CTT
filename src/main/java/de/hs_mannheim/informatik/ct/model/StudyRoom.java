package de.hs_mannheim.informatik.ct.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class StudyRoom {
    String roomName;
    String buildingName;
    int maxCapacity;
    long visitorCount;

}