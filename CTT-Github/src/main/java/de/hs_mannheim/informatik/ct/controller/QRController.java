package de.hs_mannheim.informatik.ct.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import de.hs_mannheim.informatik.ct.model.Veranstaltung;
import de.hs_mannheim.informatik.ct.persistence.services.VeranstaltungsService;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

@RestController
@RequestMapping("QRCodes")
public class QRController {
    private final String veranstaltungPath = "/besuchMitCode?vid=%s";

    private final int maxSizeInPx = 2000;

    @Autowired
    private VeranstaltungsService veranstaltungsService;

    @Value("${server.port}")
    private String port;

    @Value("${hostname}")
    private String host;

    @GetMapping(value = "/event/{eventId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] veranstaltungsCode(
            @PathVariable(name = "eventId") long veranstaltungsId,
            @RequestParam(required = false, defaultValue = "400") int width,
            @RequestParam(required = false, defaultValue = "400") int height,
            HttpServletRequest request) {
        Optional<Veranstaltung> veranstaltung = veranstaltungsService.getVeranstaltungById(veranstaltungsId);
        if (!veranstaltung.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        UriComponents qrUri = UriComponentsBuilder.newInstance()
                .scheme(request.getScheme()) // TODO: Optimally http/https should be configured somewhere
                .host(host)
                .port(port)
                .path(String.format(veranstaltungPath, veranstaltung.get().getId()))
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            QRCode.from(qrUri.toUriString())
                    .withSize(Math.min(width, maxSizeInPx), Math.min(height, maxSizeInPx))
                    .to(ImageType.PNG)
                    .writeTo(out);

            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
