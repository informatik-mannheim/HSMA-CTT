/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (c) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package de.hs_mannheim.informatik.ct.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import de.hs_mannheim.informatik.ct.model.Contact;
import de.hs_mannheim.informatik.ct.model.Visit;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.services.ContactTracingService;
import de.hs_mannheim.informatik.ct.persistence.services.DateTimeService;
import de.hs_mannheim.informatik.ct.persistence.services.DynamicContentService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import de.hs_mannheim.informatik.ct.util.ContactListGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.val;

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
            new TracingColumn("E-Mail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Matr.Nr.", contact -> contact.getContact().getEmail().split("@")[0]),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            new TracingColumn("Adresse", contact -> ""),
            new TracingColumn("Telefon", contact -> "")
    );
   
    private final List<TracingColumn> tracingColumnsStaff = Arrays.asList(
            new TracingColumn("E-Mail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Name", contact -> contact.getContact().getEmail().split("@")[0]),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            new TracingColumn("Adresse", contact -> ""),
            new TracingColumn("Telefon", contact -> "")
    );
  
    private final List<TracingColumn> tracingColumnsGuests = Arrays.asList(
            new TracingColumn("E-Mail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Name", contact -> contact.getContact().getName()),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            new TracingColumn("Adresse", contact -> contact.getContact().getAddress()),
            new TracingColumn("Telefon", contact -> contact.getContact().getNumber())
    );

    @GetMapping("/search")
    public String searchPage() {
        return "tracing/search";
    }

    @PostMapping("/results")
    public String doSearch(
            @RequestParam String email,
            @RequestParam(required=false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<Date> startDate,
            Model model) {

        val target = visitorService.findVisitorByEmail(email);
        if (!target.isPresent()) {
            model.addAttribute("error", "Eingegebene E-Mail-Adresse konnte im System nicht gefunden werden!");
            return "tracing/search";
        }

        List<Contact<? extends Visit>> contacts;
        if(startDate.isPresent()){
            contacts = contactTracingService.getVisitorContacts(target.get(), startDate.get());
        }else{
            contacts = contactTracingService.getVisitorContacts(target.get());
        }

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

        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        model.addAttribute("numberOfContacts", contacts.size());
        model.addAttribute("tableHeadersStudents", tracingColumnsStudents.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableHeadersStaff", tracingColumnsStaff.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableHeadersGuests", tracingColumnsGuests.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableValuesStudents", contactTableStudents);
        model.addAttribute("tableValuesStaff", contactTableStaff);
        model.addAttribute("tableValuesGuests", contactTableGuests);
        model.addAttribute("target", target.get().getEmail());
        model.addAttribute("searchStartDate", startDate.isPresent() ? dateFormat.format(startDate.get()) : null);

        return "tracing/contactList.html";
    }


    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadExcel(@RequestParam String email, @RequestParam String type) {
        Visitor target = visitorService.findVisitorByEmail(email)
                .orElseThrow(RoomController.RoomNotFoundException::new);

        List<Contact<?>> contacts = filterContactList(contactTracingService.getVisitorContacts(target), type);
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
    private List<Contact<?>> filterContactList(Collection<Contact<? extends Visit>> contacts, String type) {
        List<Contact<?>> filteredList = new ArrayList<>();
        for (Contact<? extends Visit> contact : contacts) {
            if (type.equals("students") && contact.getContact().getEmail().contains("@stud.hs-mannheim.de")) {
                filteredList.add(contact);
            } else if (type.equals("staff") && (contact.getContact().getEmail().contains("@hs-mannheim.de") || contact.getContact().getEmail().contains("@lba.hs-mannheim.de"))) {
                filteredList.add(contact);
            } else if (type.equals("guests") && !(contact.getContact().getEmail().contains("hs-mannheim.de"))) {
                filteredList.add(contact);
            }
        }
        return filteredList;
    }
}
