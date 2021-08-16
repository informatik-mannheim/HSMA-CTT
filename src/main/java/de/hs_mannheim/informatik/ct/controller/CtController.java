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

import de.hs_mannheim.informatik.ct.model.Event;
import de.hs_mannheim.informatik.ct.model.EventVisit;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Visitor;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


@Controller
@Slf4j
public class CtController implements ErrorController {
    @Autowired
    private EventService eventService;

    @Autowired
    private EventVisitService eventVisitService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private Utilities util;

    @Autowired
    private DynamicContentService contentService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private ContactTracingService contactTracingService;

    @Value("${server.port}")
    private String port;

    @Value("${hostname}")
    private String host;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("freeLearnerPlaces", roomVisitService.getRemainingStudyPlaces());
        return "index";
    }

    @RequestMapping("/neu") // wenn neue veranstaltung erstellt wurde
    public String newEvent(@RequestParam String name, @RequestParam Optional<Integer> max,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date datum,
                           @RequestParam String zeit,    // TODO: schauen, ob das auch eleganter geht
                           Model model, Authentication auth, @RequestHeader(value = "Referer", required = false) String referer) {

        // Optional beim int, um prüfen zu können, ob ein Wert übergeben wurde
        if (name.isEmpty() || !max.isPresent()) {                // Achtung, es gibt Java-8-Versionen, die Optional.isEmpty noch nicht enthalten!
            model.addAttribute("message", "Bitte alle mit Sternchen markierten Felder ausfüllen.");
            return "neue";
        }

        if (referer != null && (referer.endsWith("/neuVer") || referer.contains("neu?name="))) {
            datum = util.uhrzeitAufDatumSetzen(datum, zeit);

            Room defaultRoom = roomService.saveRoom(new Room("test", "test", max.get()));
            Event v = eventService.saveEvent(new Event(name, defaultRoom, datum, auth.getName()));

            return "redirect:/zeige?vid=" + v.getId(); // Qr code zeigen
        }

        return "neue";
    }

    @RequestMapping("/besuch")
    public String neuerBesuch(@RequestParam Long vid, Model model) {
        Optional<Event> event = eventService.getEventById(vid);

        if (event.isPresent()) {
            model.addAttribute("vid", event.get().getId());
            model.addAttribute("name", event.get().getName());

            return "eintragen";
        }

        model.addAttribute("error", "Event nicht gefunden");

        return home(model);
    }

    @RequestMapping("/besuchMitCode")
    public String besuchMitCode(@RequestParam Long vid, @CookieValue(value = "email", required = false) String email, Model model, HttpServletResponse response) throws UnsupportedEncodingException {
        if (email == null) {
            model.addAttribute("vid", vid);
            return "eintragen";
        }

        return besucheEintragen(vid, email, true, model, "/besuchMitCode", response);
    }

    @PostMapping("/senden")
    public String besucheEintragen(@RequestParam Long vid, @RequestParam String email, @RequestParam(required = false, defaultValue = "false") boolean saveMail, Model model,
                                   @RequestHeader(value = "Referer", required = false) String referer, HttpServletResponse response) throws UnsupportedEncodingException {

        model.addAttribute("vid", vid);

        if (referer != null && (referer.contains("/besuch") || referer.contains("/senden") || referer.contains("/besuchMitCode"))) {
            if (email.isEmpty()) {
                model.addAttribute("message", "Bitte eine Mail-Adresse eingeben.");
            } else {
                Optional<Event> event = eventService.getEventById(vid);

                if (!event.isPresent()) {
                    model.addAttribute("error", "Event nicht gefunden.");
                    model.addAttribute("email", email);
                } else {
                    int visitorCount = eventService.getVisitorCount(vid);
                    Event v = event.get();

                    if (visitorCount >= v.getRoomCapacity()) {
                        model.addAttribute("error", "Raumkapazität bereits erreicht, bitte den Raum nicht betreten.");
                    } else {
                        Visitor b = null;
                        try {
                            b = visitorService.findOrCreateVisitor(email, null, null, null);
                        } catch (InvalidEmailException e) {
                            model.addAttribute("error", "Ungültige Mail-Adresse");
                            return "eintragen";
                        } catch (InvalidExternalUserdataException e) {
                            model.addAttribute("error", "Ungültige Userdata");
                            return "eintragen";
                        }

                        Optional<String> autoAbmeldung = Optional.empty();
                        List<EventVisit> nichtAbgemeldeteBesuche = eventVisitService.signOutVisitor(b, dateTimeService.getDateNow());

                        if (nichtAbgemeldeteBesuche.size() > 0) {
                            autoAbmeldung = Optional.ofNullable(nichtAbgemeldeteBesuche.get(0).getEvent().getName());
                            // TODO: Warning in server console falls mehr als eine Event abgemeldet wurde,
                            //  da das eigentlich nicht möglich ist
                        }

                        EventVisit vb = new EventVisit(v, b, dateTimeService.getDateNow());
                        vb = eventService.saveVisit(vb);

                        if (saveMail) {
                            Cookie c = new Cookie("email", email);
                            c.setMaxAge(60 * 60 * 24 * 365 * 5);
                            c.setPath("/");
                            response.addCookie(c);
                        }

                        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                                .path("/angemeldet")
                                .queryParam("email", b.getEmail())
                                .queryParam("veranstaltungId", v.getId())
                                .queryParamIfPresent("autoAbmeldung", autoAbmeldung)
                                .build()
                                .encode(StandardCharsets.UTF_8);

                        return "redirect:" + uriComponents.toUriString();
                    } // endif Platz im Raum

                } // endif Event existiert

            } // endif nicht leere Mail-Adresse

        } // endif referer korrekt?

        return "eintragen";
    }

    @RequestMapping("/veranstaltungen")
    public String eventList(Model model) {
        Collection<Event> events = eventService.getAll();
        model.addAttribute("events", events);

        return "eventList";
    }

    @RequestMapping("/zeige")
    public String showEvent(@RequestParam Long vid, Model model) {
        Optional<Event> event = eventService.getEventById(vid);

        if (event.isPresent()) {
            model.addAttribute("event", event.get());
            model.addAttribute("teilnehmerzahl", eventService.getVisitorCount(vid));

            return "event";
        }

        model.addAttribute("error", "Event nicht gefunden!");

        return home(model);
    }


    @RequestMapping("/angemeldet")
    public String angemeldet(
            @RequestParam String email, @RequestParam(value = "veranstaltungId") long eventId,
            @RequestParam(required = false) Optional<String> autoAbmeldung, Model model, HttpServletResponse response) {

        Optional<Event> v = eventService.getEventById(eventId);

        if (!v.isPresent()) {
            model.addAttribute("error", "Event nicht gefunden!");
            return "index";
        }

        model.addAttribute("visitorEmail", email);
        model.addAttribute("autoAbmeldung", autoAbmeldung.orElse(""));

        model.addAttribute("message", "Vielen Dank, Sie wurden erfolgreich im Raum eingecheckt.");

        Cookie c = new Cookie("checked-into", "" + v.get().getId());    // Achtung, Cookies erlauben keine Sonderzeichen (inkl. Whitespaces)!
        c.setMaxAge(60 * 60 * 8);
        c.setPath("/");
        response.addCookie(c);

        return "angemeldet";
    }

    @RequestMapping("/abmelden")
    public String abmelden(@RequestParam(name = "besucherEmail", required = false) String besucherEmail,
                           Model model, HttpServletRequest request, HttpServletResponse response, @CookieValue("email") String mailInCookie) {

        // TODO: ich denke, wir müssen das Speichern der Mail im Cookie zur Pflicht machen, wenn wir den Logout über die Leiste oben machen wollen?
        // Oder wir versuchen es mit einer Session-Variablen?

        if (besucherEmail == null || besucherEmail.length() == 0) {
            if (mailInCookie != null && mailInCookie.length() > 0)
                besucherEmail = mailInCookie;
            else
                return "index";
        }

        visitorService.findVisitorByEmail(besucherEmail)
                .ifPresent(value -> eventVisitService.signOutVisitor(value, dateTimeService.getDateNow()));

        Cookie c = new Cookie("checked-into", "");
        c.setMaxAge(0);
        c.setPath("/");
        response.addCookie(c);

        return "abgemeldet";
    }

    // zum Testen ggf. wieder aktivieren
    //	@RequestMapping("/loeschen")
    //	public String kontakteLoeschen(Model model) {
    //		vservice.loescheAlteBesuche();
    //		model.addAttribute("message", "Alte Kontakte gelöscht!");
    //
    //		return "index";
    //	}

    @RequestMapping("/neuVer")
    public String neu() {
        return "neue";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/datenschutz")
    public String datenschutz() {
        return "datenschutz";
    }

    @RequestMapping("/howToQr")
    public String howToQr() {
        return "howToQr";
    }

    @RequestMapping("/howToInkognito")
    public String howToInkognito() {
        return "howToInkognito";
    }

    @RequestMapping("/learningRooms")
    public String showLearningRooms(Model model) {
        model.addAttribute("learningRoomsCapacity", roomVisitService.getAllStudyRooms());
        return "learningRooms";
    }

    @RequestMapping("/faq")
    public String showFaq(Model model) {
        String[] questions1 = {"Wie kann ich mich einchecken?",
                "Wie kann ich mich auschecken?",
                "Ich habe kein Smartphone zur Verfügung, wie kann ich mich trotzdem einchecken?",
                "Kann ich auch andere einchecken?",
                "Kann ich mich auch später ein- bzw. auschecken?",
                "Kann ich auch ohne Kontaktverfolgung an Veranstaltungen der Hochschule teilnehmen?",
                "Was ist, wenn ich vergessen habe mich auszuchecken?",
                " Ich habe Corona und war in den vergangenen Tagen an der Hochschule, was muss ich jetzt tun?"};
        String[] answers1 = {"<p>Du musst lediglich mit deinem Smartphone den QR-Code abscannen, dann wirst du automatisch zum Check in für den passenden Raum weitergeleitet. Alternativ, wenn du keinen QR-Scanner zur Verfügung hast, kannst du auch manuell die URL in einen beliebigen Browser eingeben. Eine etwas ausführlichere Antwort findest du <a href='https://ctt.hs-mannheim.de/howToQr'>hier</a>.</p>",
                "<p>Das Auschecken funktioniert genau so, wie das Einchecken. Du musst lediglich den QR-Code abscannen und dann ganz unten auf den Button Abmelden klicken. Dann wirst du automatisch zum Check-out weitergeleitet. Eine ausführliche Antwort findest du <a href='https://ctt.hs-mannheim.de/howToQr'>hier</a>.</p>",
                "<p>Auf jedem Zettel befindet sich oberhalb des QR Codes eine URL. Du kannst auch einfach in deinem Browser die URL eintippen und dich so beispielsweise mit deinem Laptop oder Tablet einchecken. Falls du gar kein Gerät zur Verfügung hast bitte jemand anderen dich einzuchecken. Eine ausführliche Anleitung dazu findest du <a href='https://ctt.hs-mannheim.de/howToInkognito'>hier</a>.</p>",
                "<p>Ja, das geht natürlich auch, per Handy, Tablet oder Laptop. Öffne dazu einfach ein weiteres Fenster in deinem Browser und rufe die URL auf dem Zettel auf. Anschließend, einfach die Daten der Person eintragen. Der Einfachheitshalber kannst du das Fenster für die Veranstaltungsdauer direkt für das Auschecken offen lassen. Eine etwas ausführlichere Beschreibung findest du <a href='https://ctt.hs-mannheim.de/howToInkognito'>hier</a>.</p>",
                "<p>Nein, es ist sehr wichtig dass das ein uns auschecken auch immer genau dann passiert, wenn du den Raum betrittst oder verlässt, sonst werden im System falsche Uhrzeiten gespeichert.</p>",
                "<p>Nein,  leider geht das aktuell nur bei Online Veranstaltungen.</p>",
                "<p>Du wirst automatisch ausgecheckt, wenn du einen anderen Raum betrittst. Notfalls checkt dich das System abends aus. Für die Kontaktverfolgung warst du dann leider noch im Raum.</p>",
                " <p>Wenn du positiv getestet wurdest ist es sehr wichtig, dass du eine E-Mail an <a href='javascript:linkTo_UnCryptMailto('nbjmup;dpspob/{wAit.nbooifjn/ef')'>corona.zv@hs-mannheim.de</a> schreibst und darin deine Kontaktdaten (Name, Anschrift, Telefonnummer, Geburtsdatum) sowie das Datum des positiven Tests mitteilst.</p>"};
        ArrayList<String> answers = getFAQText().get("answers");
        ArrayList<String> questions = getFAQText().get("questions");
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        return "faq";
    }

    public Map<String, ArrayList<String>> getFAQText() {
        // refactor naming, email link bug, anleitung
        Map<String, ArrayList<String>> faqs = new HashMap<>();
        String FILENAME = "src/main/resources/static/faq.xml";
        ArrayList<String> answers = new ArrayList<>();
        ArrayList<String> questions = new ArrayList<>();
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(FILENAME));
            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            // get <staff>
            NodeList list = doc.getElementsByTagName("faq-element");

            for (int temp = 0; temp < list.getLength(); temp++) {

                Node node = list.item(temp);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;

                    // get staff's attribute
                    String id = element.getAttribute("id");

                    // get text
                    String question = element.getElementsByTagName("question").item(0).getTextContent();
                    String answer = element.getElementsByTagName("answer").item(0).getTextContent();
// get complete content and add to arraylist, then return list
                    answers.add(answer);
                    questions.add(question);


                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        faqs.put("answers", answers);
        faqs.put("questions", questions);
        return faqs;
    }

    ;

    // ------------
    // ErrorControllerImpl
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int code = Integer.parseInt(status.toString());
            log.error("Web ErrorCode: " + code);
            log.error("URL:" + request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString());
            if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString().equals("/r/noId")) {
                log.error("Der Raum konnte nicht gefunden werden");
            }
            if (code == HttpStatus.FORBIDDEN.value())
                model.addAttribute("error", "Zugriff nicht erlaubt. Evtl. mit einer falschen Rolle eingeloggt?");
            else if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString().equals("/r/noId")) {
                model.addAttribute("error", "Diesen Raum gibt es nicht");
            } else if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString().equals("/r/checkOut")) {
                log.error("Checkout nicht möglich da Email nicht vorhanden");
                model.addAttribute("error", "Checkout nicht möglich, Emailadresse nicht im System. Waren Sie eingecheckt?");
            } else
                model.addAttribute("error", "Fehler-Code: " + status);
        } else {
            model.addAttribute("error", "Unbekannter Fehler!");
        }

        return home(model);
    }

    @Override
    public String getErrorPath() {
        return null;
    }
}
