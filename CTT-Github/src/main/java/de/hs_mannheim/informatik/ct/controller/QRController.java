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

import de.hs_mannheim.informatik.ct.persistence.services.DynamicContentService;
import de.hs_mannheim.informatik.ct.persistence.services.RoomService;
import de.hs_mannheim.informatik.ct.persistence.services.EventService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;



@RestController
@RequestMapping("QRCodes")
public class QRController {
    private final String veranstaltungPath = "/besuchMitCode?vid=%s";

    private final int maxSizeInPx = 2000;

    @Autowired
    private EventService eventService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private DynamicContentService contentService;

    @Autowired
    private Utilities utilities;

    @Value("${server.port}")
    private String port;

    @Value("${hostname}")
    private String host;

    @GetMapping(value = "/room/{roomId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getRoomQRCode(
            @PathVariable(name = "roomId") String roomId,
            @RequestParam(required = false, defaultValue = "400") int width,
            @RequestParam(required = false, defaultValue = "400") int height,
            HttpServletRequest request
    ) {
        val room = roomService.findByName(roomId);
        if (!room.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        val qrUri = utilities.getUriToLocalPath(
                RoomController.getRoomCheckinPath(room.get()),
                request
        );

        return getQRImage(qrUri, width, height);
    }

    @GetMapping(value = "/event/{eventId}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] eventQRCode(
            @PathVariable long eventId,
            @RequestParam(required = false, defaultValue = "400") int width,
            @RequestParam(required = false, defaultValue = "400") int height,
            HttpServletRequest request) {
        val event = eventService.getEventById(eventId);
        if (!event.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        val qrUri = utilities.getUriToLocalPath(String.format(veranstaltungPath, event.get().getId()), request);

        return getQRImage(qrUri, width, height);
    }

    private byte[] getQRImage(UriComponents uri, int requestedWidth, int requestedHeight) {
        return contentService.getQRCodePNGImage(uri, Math.min(requestedWidth, maxSizeInPx), Math.min(requestedHeight, maxSizeInPx));
    }
}
