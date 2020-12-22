package de.hs_mannheim.informatik.ct.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import de.hs_mannheim.informatik.ct.model.Besucher;
import de.hs_mannheim.informatik.ct.model.Room;
import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuch;
import de.hs_mannheim.informatik.ct.model.VeranstaltungsBesuchDTO;
import de.hs_mannheim.informatik.ct.persistence.services.VeranstaltungsBesuchService;
import de.hs_mannheim.informatik.ct.persistence.services.VeranstaltungsService;

@Controller
public class CtController implements ErrorController {
	@Autowired
	private VeranstaltungsService vservice;

	@Autowired
	private VeranstaltungsBesuchService veranstaltungsBesuchService;

	@Autowired
	private Utilities util;

	@Value("${server.port}")
	private String port;

	@Value("${hostname}")
	private String host;

	@RequestMapping("/")
	public String home(Model model) {
		Collection<Veranstaltung> veranstaltungen = vservice.findeAlleHeutigenVeranstaltungen();
		model.addAttribute("veranstaltungen", veranstaltungen);

		return "index";
	}

	@RequestMapping("/neu")
	public String neueVeranstaltung(@RequestParam String name, @RequestParam Optional<Integer> max, 
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date datum,
			@RequestParam String zeit,	// TODO: schauen, ob das auch eleganter geht
			Model model, Authentication auth, @RequestHeader(value = "Referer", required = false) String referer) {

		// Optional beim int, um prüfen zu können, ob ein Wert übergeben wurde
		if (name.isEmpty() || !max.isPresent()) {				// Achtung, es gibt Java-8-Versionen, die Optional.isEmpty noch nicht enthalten!
			model.addAttribute("message", "Bitte alle mit Sternchen markierten Felder ausfüllen.");
			return "neue";
		}

		if (referer != null && (referer.endsWith("/neuVer") || referer.contains("neu?name="))) {
			datum = util.uhrzeitAufDatumSetzen(datum, zeit);

			// TODO irgendwas stimmt mit der ManyToOne-Relation von Veranstaltung zu Raum noch nicht
			// das Math.random ist ein übler Workaround
			Veranstaltung v = vservice.speichereVeranstaltung(new Veranstaltung(name, new Room("test" + (int)(Math.random() * 10000000), max.get()), datum, auth.getName()));

			return "redirect:/zeige?vid=" + v.getId();
		}

		return "neue";
	}

	@RequestMapping("/besuch")
	public String neuerBesuch(@RequestParam Long vid, Model model) {
		Optional<Veranstaltung> v = vservice.getVeranstaltungById(vid);

		if (v.isPresent()) {
			model.addAttribute("vid", v.get().getId());
			model.addAttribute("name", v.get().getName());

			return "eintragen";
		}

		model.addAttribute("error", "Veranstaltung nicht gefunden");

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
			if (email.isEmpty() || !util.checkMailAdressSyntax(email)) {
				model.addAttribute("message", "Bitte eine Mail-Adresse eingeben.");
			} else {
				Optional<Veranstaltung> vo = vservice.getVeranstaltungById(vid);

				if (!vo.isPresent()) {
					model.addAttribute("error", "Veranstaltung nicht gefunden.");
					model.addAttribute("email", email);
				} else {
					int besucherZahl = vservice.getBesucherAnzahl(vid);
					Veranstaltung v = vo.get();

					if (besucherZahl >= v.getRaumkapazitaet()) {
						model.addAttribute("error", "Raumkapazität bereits erreicht, bitte den Raum nicht betreten.");
					} else {
						Besucher b = vservice.getBesucherByEmail(email);

						if (b == null) {
							b = new Besucher(email);
							b = vservice.speichereBesucher(b);
						}

						Optional<String> autoAbmeldung = Optional.empty();
						List<VeranstaltungsBesuch> nichtAbgemeldeteBesuche = veranstaltungsBesuchService.besucherAbmelden(b, new Date());

						if(nichtAbgemeldeteBesuche.size() > 0) {
							autoAbmeldung = Optional.ofNullable(nichtAbgemeldeteBesuche.get(0).getVeranstaltung().getName());
							// TODO: Warning in server console falls mehr als eine Veranstaltung abgemeldet wurde,
							//  da das eigentlich nicht möglich ist
						}

						VeranstaltungsBesuch vb = new VeranstaltungsBesuch(v, b);
						vb = vservice.speichereBesuch(vb);

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
					
				} // endif Veranstaltung existiert
				
			} // endif nicht leere Mail-Adresse
			
		} // endif referer korrekt?

		return "eintragen";
	}

	@RequestMapping("/veranstaltungen")
	public String veranstaltungenAnzeigen(Model model) {
		Collection<Veranstaltung> veranstaltungen = vservice.findeAlleVeranstaltungen();
		model.addAttribute("veranstaltungen", veranstaltungen);

		return "veranstaltungsliste";
	}

	@RequestMapping("/zeige")
	public String zeigeVeranstaltung(@RequestParam Long vid, Model model) {
		Optional<Veranstaltung> v = vservice.getVeranstaltungById(vid);

		if (v.isPresent()) {
			model.addAttribute("veranstaltung", v.get());
			model.addAttribute("teilnehmerzahl", vservice.getBesucherAnzahl(vid));

			return "veranstaltung";
		}

		model.addAttribute("error", "Veranstaltung nicht gefunden!");

		return home(model);
	}

	@RequestMapping("/suchen")
	public String kontakteFinden(@RequestParam String email, Model model) {
		Collection<VeranstaltungsBesuchDTO> kontakte = vservice.findeKontakteFuer(email);

		model.addAttribute("kranker", email);
		model.addAttribute("kontakte", kontakte);

		return "kontaktliste";
	}

	@RequestMapping("/download")
	public void kontakteHerunterladen(@RequestParam String email, HttpServletResponse response) {
		Collection<VeranstaltungsBesuchDTO> kontakte = vservice.findeKontakteFuer(email);

		response.setHeader("Content-disposition", "attachment; filename=kontaktliste.xls");
		response.setContentType("application/vnd.ms-excel");

		try(OutputStream out = response.getOutputStream()) {
			util.excelErzeugen(kontakte, email).write(out);
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: wie kann man dem User Bescheid geben, falls doch mal etwas schief gehen sollte?
		}
	}

	@RequestMapping("/angemeldet")
	public String angemeldet(
			@RequestParam String email, @RequestParam long veranstaltungId, 
			@RequestParam(required = false) Optional<String> autoAbmeldung, Model model, HttpServletResponse response) {

		Optional<Veranstaltung> v = vservice.getVeranstaltungById(veranstaltungId);

		if (!v.isPresent()) {
			model.addAttribute("error", "Veranstaltung nicht gefunden!");
			return "index";
		}
		
		model.addAttribute("besucherEmail", email);
		model.addAttribute("veranstaltungId", veranstaltungId);
		model.addAttribute("autoAbmeldung", autoAbmeldung.orElse(""));

		model.addAttribute("message", "Vielen Dank, Sie wurden erfolgreich im Raum eingecheckt.");
		
		Cookie c = new Cookie("checked-into", v.get().getName());
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
			
		veranstaltungsBesuchService.besucherAbmelden(vservice.getBesucherByEmail(besucherEmail), new Date());

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

	@RequestMapping("/suche")
	public String suche() {
		return "suche";
	}

	@RequestMapping("/login")
	public String login() {
		return "login";
	}

	@RequestMapping("/datenschutz")
	public String datenschutz() {
		return "datenschutz";
	}

	// ------------
	// ErrorControllerImpl
	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			int code = Integer.parseInt(status.toString());

			if (code == HttpStatus.FORBIDDEN.value())
				model.addAttribute("error", "Zugriff nicht erlaubt. Evtl. mit einer falschen Rolle eingeloggt?");
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
