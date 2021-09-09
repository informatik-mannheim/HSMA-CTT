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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import de.hs_mannheim.informatik.ct.model.Contact;
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

    private final List<TracingColumn> tracingColumns = Arrays.asList(
            new TracingColumn("EMail-Adresse", contact -> contact.getContact().getEmail()),
            new TracingColumn("Raum/Veranstaltung", Contact::getContactLocation),
            new TracingColumn("Datum", contact -> dateFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getStartDate())),
            new TracingColumn("Anmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getStartDate())),
            new TracingColumn("Abmeldung Ziel", contact -> timeFormatter.format(contact.getTargetVisit().getEndDate())),
            new TracingColumn("Abmeldung Kontakt", contact -> timeFormatter.format(contact.getContactVisit().getEndDate()))
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

        model.addAttribute("tableHeaders", tracingColumns.stream().map(TracingColumn::getHeader).toArray());
        model.addAttribute("tableValues", contactTable);
        model.addAttribute("target", target.get().getEmail());

        return "tracing/contactList.html";
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadExcel(@RequestParam String email) {
        val target = visitorService.findVisitorByEmail(email)
                .orElseThrow(RoomController.RoomNotFoundException::new);

        val contacts = contactTracingService.getVisitorContacts(target);

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
}
