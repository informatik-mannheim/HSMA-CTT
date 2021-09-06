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

    private final List<TracingColumn> tracingColumns = Arrays.asList(
            new TracingColumn("EMail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Mtknr.", contact -> contact.getContact().getEmail().split("@")[0]),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            // Is it okay to do it like this? more like a workaround from the original datatype
            new TracingColumn( "Geburtsdatum", contact -> "" ),
            new TracingColumn( "PLZ", contact -> ""),
            new TracingColumn( "Ort", contact -> ""),
            new TracingColumn( "Str., Hausnr.", contact -> "")
    );
    private final List<TracingColumn> tracingColumnsStaff = Arrays.asList(
            new TracingColumn("EMail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Nachname", contact -> getNameOfStaff(contact.getContact().getEmail())),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            // Is it okay to do it like this? more like a workaround from the original datatype
            new TracingColumn( "Geburtsdatum", contact -> "" ),
            new TracingColumn( "PLZ", contact -> ""),
            new TracingColumn( "Ort", contact -> ""),
            new TracingColumn( "Str., Hausnr.", contact -> "")
    );
    private final List<TracingColumn> tracingColumnsGuests = Arrays.asList(
            new TracingColumn("EMail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Nachname", contact -> getName(contact.getTargetVisit().getVisitor())),
            new TracingColumn("Vorname", contact -> contact.getContact().getEmail().split("@")[0]),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate())),
            // Is it okay to do it like this? more like a workaround from the original datatype
            new TracingColumn( "Geburtsdatum", contact -> "" ),
            new TracingColumn( "PLZ", contact -> ""),
            new TracingColumn( "Ort", contact -> ""),
            new TracingColumn( "Str., Hausnr.", contact -> "")
    );

    @GetMapping("/search")
    public String searchPage() {
        return "tracing/search";
    }
    private String getName (Visitor visitor){
      ExternalVisitor externalVisitor = (ExternalVisitor) visitor;
      return externalVisitor.getName();
    };

    @PostMapping("/results")
    public String doSearch(@RequestParam String email, Model model) throws InvalidEmailException, InvalidExternalUserdataException {
        val target = visitorService.findVisitorByEmail(email);
      /*  if(!target.isPresent()){
            visitorService.findOrCreateVisitor(email, "test", "101", "test");
        }
        val target2 = visitorService.findVisitorByEmail(email);*/
        if (!target.isPresent()) {
            System.out.println("Email adresse nicht registriert: "+email);

            model.addAttribute("error", "Eingegebene Mail-Adresse nicht gefunden!");
            return "tracing/search";
        }

        val contacts = contactTracingService.getVisitorContacts(target.get());
        ArrayList<String> mails = new ArrayList<>();


        // Liste nach hs mail adressen filtern
        // weiter unterteilen in mailadressen mit buchstaben und zahlen
        // Für alle 3 Listen helfer methode für verschachtelte Liste
        List<String[]> contactTable = contacts
                .stream()
                .map(contact -> {
                    val rowValues = new String[tracingColumns.size()];
                    for (int columnIndex = 0; columnIndex < tracingColumns.size(); columnIndex++) {
                        val column = tracingColumns.get(columnIndex);
                        rowValues[columnIndex] = column.cellValue.apply(contact);
                    }
                    return rowValues;
                }).collect(Collectors.toList());

        HashMap<String, ArrayList<String[]>> contactLists = separeteContactList(contactTable);
        ArrayList<String[]> contactListStudents = contactLists.get("students");
        ArrayList<String[]> contactListsStaff = contactLists.get("universityStaff");
        ArrayList<String[]> contactListGuests = contactLists.get("guests");
        // pass lists to frontend and display for user
        // margin footer
        // adapt downloadable excel sheets to frontend => all data in one file with 3 different sheets vs. 3 files?
        model.addAttribute("tableHeaders", tracingColumns.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableValues", contactTable);
        model.addAttribute("tableValuesStudents", contactListStudents);
        model.addAttribute("tableValuesStaff", contactListsStaff);
        model.addAttribute("tableValuesGuests", contactListGuests);
        model.addAttribute("target", target.get().getEmail());

        return "tracing/contactList.html";
    }
    // which map best use?
    // Helper method to seperate contact list in 3 different separate Lists
    private HashMap<String, ArrayList<String[]>> separeteContactList (List<String[]> contactList){
        HashMap<String, ArrayList<String[]>> contactListSeparated = new HashMap<String, ArrayList<String[]>>();
        ArrayList<String[]> students = new ArrayList<String[]>();
        ArrayList<String[]> universityStaff = new ArrayList<String[]>();
        ArrayList<String[]> guests = new ArrayList<String[]>();
        for(String[] contact: contactList){
            if(contact[0].contains("@stud.hs-mannheim.de")){
                students.add(contact);
            }else if(contact[0].contains("@hs-mannheim.de")){
                universityStaff.add(contact);
            }else{
                guests.add(contact);
            }
        }
        contactListSeparated.put("students", students);
        contactListSeparated.put("universityStaff", universityStaff);
        contactListSeparated.put("guests", guests);
        return contactListSeparated;
    }
    // download in 3 exceltabellen unterteilen
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadExcel(@RequestParam String email, @RequestParam String type) {
        val target = visitorService.findVisitorByEmail(email)
                .orElseThrow(RoomController.RoomNotFoundException::new);

        val contacts = filterContactList(contactTracingService.getVisitorContacts(target), type);
       // contacts.get(1).getTargetVisit().getVisitor().getEmail();
        Visitor vis= target;
        ExternalVisitor vi = (ExternalVisitor) target;
    vi.getAddress();

        // generating header
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
    // helper method to filter contactList
    private Collection<Contact<?>> filterContactList(Collection<Contact<?>> contacts, String type){
        List<Contact<?>> deleteList = new ArrayList<>();
        System.out.println("all contacts "+contacts.toString());
        for(Contact contact: contacts){
            if(type.equals("students") && contact.getContact().getEmail().contains("@stud.hs-mannheim.de")){
                deleteList.add(contact);
            }else if(type.equals("staff") && contact.getContact().getEmail().contains("@hs-mannheim.de")){
                deleteList.add(contact);
            }else if(type.equals("students") && !(contact.getContact().getEmail().contains("@stud.hs-mannheim.de")) && !(contact.getContact().getEmail().contains("@hs-mannheim.de"))){
                deleteList.add(contact);
            }
        }
        System.out.println("Delete list: "+deleteList.toString());
        contacts.removeAll(deleteList);
        System.out.println("removed contactlist "+contacts.toString());
        return contacts;
    }

    private String getNameOfStaff (String email){
        String name = email.split("@")[0];
        if(email.contains(".")){
            email = email.split(".")[1];
        }
        return name;
    }
}
