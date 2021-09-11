package de.hs_mannheim.informatik.ct.controller;

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

import ch.qos.logback.core.net.SyslogOutputStream;
import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.model.ExternalVisitor;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.model.ExternalVisitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.services.ContactTracingService;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.DynamicContentService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import de.hs_mannheim.informatik.ct.util.ContactListGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("tracing")
public class ContactTracingController {
    @Autowired
    private VisitorService visitorService;

    @Autowired
    private ContactTracingService contactTracingService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private DynamicContentService dynamicContentService;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

    private final List<TracingColumn> tracingColumnsStudents = Arrays.asList(
            new TracingColumn("EMail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Mtknr.", contact -> contact.getContact().getEmail().split("@")[0]),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            new TracingColumn("Geburtsdatum", contact -> ""),
            new TracingColumn("Straße", contact -> ""),
            new TracingColumn("Hausnummer", contact -> ""),
            new TracingColumn("Plz", contact -> ""),
            new TracingColumn("Ort", contact -> ""),
            new TracingColumn("Tel.", contact -> "")
    );
    private final List<TracingColumn> tracingColumnsStaff = Arrays.asList(
            new TracingColumn("EMail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Name", contact -> contact.getContact().getEmail().split("@")[0]),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            new TracingColumn("Geburtsdatum", contact -> ""),
            new TracingColumn("Straße", contact -> ""),
            new TracingColumn("Hausnummer", contact -> ""),
            new TracingColumn("Plz", contact -> ""),
            new TracingColumn("Ort", contact -> ""),
            new TracingColumn("Tel.", contact -> "")
    );
    private final List<TracingColumn> tracingColumnsGuests = Arrays.asList(
            new TracingColumn("EMail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Nachname", contact -> contact.getContact().getName().split(" ")[0]),
            new TracingColumn("Vorname", contact -> contact.getContact().getName().split(" ")[1]),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            new TracingColumn("Geburtsdatum", contact -> ""),
            // If the adress was not put in the format straße hausnummer plz ort an exception is thrown, to avoid this an empty string gets returned. Better solution would be to to adapt the form so that only adress in the corect format can be send
            new TracingColumn("Straße", contact -> contact.getContact().getAddress().split(" ")[0]),
            new TracingColumn("Hausnummer", contact -> {
                if (contact.getContact().getAddress().split(" ").length > 0) {
                    return contact.getContact().getAddress().split(" ")[1];
                } else {
                    return "";
                }
            }),
            new TracingColumn("Plz", contact -> {
                if (contact.getContact().getAddress().split(" ").length > 2) {
                    return contact.getContact().getAddress().split(" ")[2];
                } else {
                    return " ";
                }
            }),
            new TracingColumn("Ort", contact -> {
                if (contact.getContact().getAddress().split(" ").length > 3) {
                    return contact.getContact().getAddress().split(" ")[3];
                } else {
                    return "";
                }
            }),
            new TracingColumn("Tel.", contact -> contact.getContact().getNumber())
    );

    @GetMapping("/search")
    public String searchPage() {
        return "tracing/search";
    }

    @PostMapping("/results")
    public String doSearch(@RequestParam String email, Model model) {
        val target = visitorService.findVisitorByEmail(email);
        if (!target.isPresent()) {
            model.addAttribute("error", "Eingegebene Mail-Adresse nicht gefunden!");
            return "tracing/search";
        }

        val contacts = contactTracingService.getVisitorContacts(target.get());
        List<Contact<?>> contactsStudents = filterContactList(contacts, "students");
        List<Contact<?>> contactsStaff = filterContactList(contacts, "staff");
        List<Contact<?>> contactsGuests = filterContactList(contacts, "guests");

        List<String[]> contactTableStudents = contactsStudents
                .stream()
                .map(contact -> {
                    val rowValues = new String[tracingColumnsStudents.size()];
                    for (int columnIndex = 0; columnIndex < tracingColumnsStudents.size(); columnIndex++) {
                        val column = tracingColumnsStudents.get(columnIndex);
                        rowValues[columnIndex] = column.cellValue.apply(contact);
                    }
                    return rowValues;
                }).collect(Collectors.toList());
        List<String[]> contactTableStaff = contactsStaff
                .stream()
                .map(contact -> {
                    val rowValues = new String[tracingColumnsStaff.size()];
                    for (int columnIndex = 0; columnIndex < tracingColumnsStaff.size(); columnIndex++) {
                        val column = tracingColumnsStaff.get(columnIndex);
                        rowValues[columnIndex] = column.cellValue.apply(contact);
                    }
                    return rowValues;
                }).collect(Collectors.toList());

        List<String[]> contactTableGuests = contactsGuests
                .stream()
                .map(contact -> {
                    val rowValues = new String[tracingColumnsGuests.size()];
                    for (int columnIndex = 0; columnIndex < tracingColumnsGuests.size(); columnIndex++) {
                        val column = tracingColumnsGuests.get(columnIndex);
                        rowValues[columnIndex] = column.cellValue.apply(contact);
                    }
                    return rowValues;
                }).collect(Collectors.toList());

        System.out.println("Contactsize: " + contacts.size());

        model.addAttribute("numberOfContacts", contacts.size());
        model.addAttribute("tableHeadersStudents", tracingColumnsStudents.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableHeadersStaff", tracingColumnsStaff.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableHeadersGuests", tracingColumnsGuests.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableValuesStudents", contactTableStudents);
        model.addAttribute("tableValuesStaff", contactTableStaff);
        model.addAttribute("tableValuesGuests", contactTableGuests);
        model.addAttribute("target", target.get().getEmail());

        return "tracing/contactList.html";
    }


    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadExcel(@RequestParam String email, @RequestParam String type) {
        val target = visitorService.findVisitorByEmail(email)
                .orElseThrow(RoomController.RoomNotFoundException::new);

        val contacts = filterContactList(contactTracingService.getVisitorContacts(target), type);
        List<ContactTracingController.TracingColumn> tracingColumns = tracingColumnsStudents;
        if (type.equals("staff")) {
            tracingColumns = tracingColumnsStaff;
        } else if (type.equals("guests")) {
            tracingColumns = tracingColumnsGuests;
        }

        val generator = new ContactListGenerator(
                dateTimeService,
                tracingColumns.stream().map(TracingColumn::getHeader).collect(Collectors.toList()),
                tracingColumns.stream().map(TracingColumn::getCellValue).collect(Collectors.toList())
        );

        StreamingResponseBody responseBody = outputStream -> {
            try {
                dynamicContentService.writeContactList(contacts, target, generator, outputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=kontaktliste.xlsx")
                .contentType(MediaType.valueOf("application/vnd.ms-excel"))
                .body(responseBody);
    }

    @Data
    @AllArgsConstructor
    public static class TracingColumn {
        private String header;
        private Function<Contact<?>, String> cellValue;
    }


    /**
     * Helper method to filter the contacts into 3 subcategories: students, employees of the university and guests
     *
     * @param contacts List of all possible contacts of the target email
     * @param type     String of the filter type (students/ staff/ guests)
     * @return filtered List of Contacts
     */
    private List<Contact<?>> filterContactList(Collection<Contact<?>> contacts, String type) {
        List<Contact<?>> filteredList = new ArrayList<>();
        for (Contact contact : contacts) {
            if (type.equals("students") && contact.getContact().getEmail().contains("@stud.hs-mannheim.de")) {
                filteredList.add(contact);
            } else if (type.equals("staff") && contact.getContact().getEmail().contains("@hs-mannheim.de")) {
                filteredList.add(contact);
            } else if (type.equals("guests") && !(contact.getContact().getEmail().contains("@stud.hs-mannheim.de")) && !(contact.getContact().getEmail().contains("@hs-mannheim.de"))) {
                filteredList.add(contact);
            }
        }
        return filteredList;
    }
}
