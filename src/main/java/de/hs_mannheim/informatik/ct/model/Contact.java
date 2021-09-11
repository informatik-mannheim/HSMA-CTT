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
    } // 'infizierte Person'

    public Visitor getContact() { return contactVisit.getVisitor(); } // Kontakte der 'infizierten Person'

    public String getContactLocation() {
        assert targetVisit.getLocationName().equals(contactVisit.getLocationName());
        return targetVisit.getLocationName();
    }
}
