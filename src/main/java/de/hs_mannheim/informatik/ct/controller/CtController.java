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

import de.hs_mannheim.informatik.ct.model.*;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.services.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


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
		return "index";
	}

	@RequestMapping("/neu") // wenn neue veranstaltung erstellt wurde
	public String newEvent(@RequestParam String name, @RequestParam Optional<Integer> max,
						   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date datum,
						   @RequestParam String zeit,    // TODO: schauen, ob das auch eleganter geht
						   Model model, Authentication auth, @RequestHeader(value = "Referer", required = false) String referer) {

		// Optional beim int, um prüfen zu können, ob ein Wert übergeben wurde
		if (name.isEmpty() || !max.isPresent()) {				// Achtung, es gibt Java-8-Versionen, die Optional.isEmpty noch nicht enthalten!
			model.addAttribute("message", "Bitte alle mit Sternchen markierten Felder ausfüllen.");
			return "neue";
		}

		if (referer != null && (referer.endsWith("/neuVer") || referer.contains("neu?name="))) {
			datum = util.uhrzeitAufDatumSetzen(datum, zeit);

			Room defaultRoom = roomService.saveRoom(new Room("test","test", max.get()));
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
	public String besuchMitCode(@RequestParam Long vid, @CookieValue(value="email", required=false) String email, Model model, HttpServletResponse response) throws UnsupportedEncodingException {
		if (email == null) {
			model.addAttribute("vid", vid);
			return "eintragen";
		}

		return besucheEintragen(vid, email, true, model, "/besuchMitCode", response);
	}

	@PostMapping("/senden")
	public String besucheEintragen(@RequestParam Long vid, @RequestParam String email, @RequestParam(required = false, defaultValue="false") boolean saveMail, Model model,
			@RequestHeader(value = "Referer", required = false) String referer, HttpServletResponse response) throws UnsupportedEncodingException {

		model.addAttribute("vid", vid);

		if (referer != null && (referer.contains("/besuch") || referer.contains("/senden") || referer.contains("/besuchMitCode")) ) {
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

						if(nichtAbgemeldeteBesuche.size() > 0) {
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

		Cookie c = new Cookie("checked-into", "" + v.get().getId());	// Achtung, Cookies erlauben keine Sonderzeichen (inkl. Whitespaces)!
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

	// ------------
	// ErrorControllerImpl
	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			int code = Integer.parseInt(status.toString());
			log.error("Web ErrorCode: " + code);
			log.error("URL:" +  request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString());
			if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString().equals("/r/noId")){
				log.error("Der Raum konnte nicht gefunden werden");
			}
			if (code == HttpStatus.FORBIDDEN.value())
				model.addAttribute("error", "Zugriff nicht erlaubt. Evtl. mit einer falschen Rolle eingeloggt?");
			else if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString().equals("/r/noId")){
				model.addAttribute("error", "Diesen Raum gibt es nicht");
			}
			else if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI).toString().equals("/r/checkOut")){
                log.error("Checkout nicht möglich da Email nicht vorhanden");
                model.addAttribute("error", "Checkout nicht möglich, Emailadresse nicht im System. Waren Sie eingecheckt?");
            }
			else
				model.addAttribute("error", "Fehler-Code: " + status.toString());
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
