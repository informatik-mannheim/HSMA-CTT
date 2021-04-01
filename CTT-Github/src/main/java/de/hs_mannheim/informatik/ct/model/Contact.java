package de.hs_mannheim.informatik.ct.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.util.Date;

@Data
@AllArgsConstructor
public class Contact<T extends Visit> {
    private T targetVisit;
    private T contactVisit;

    public Visitor getTarget() {
        return targetVisit.getVisitor();
    }

    public String getContactLocation() {
        assert targetVisit.getLocationName().equals(contactVisit.getLocationName());
        return targetVisit.getLocationName();
    }

    @Deprecated
    public Date getStartDate() {
        return targetVisit.getStartDate();
    }

    @Deprecated
    public Duration getDiffInStart() {
        return Duration.between(targetVisit.getStartDate().toInstant(), contactVisit.getStartDate().toInstant()).abs();
    }
}
