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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.hs_mannheim.informatik.ct.controller.exception.InvalidRoomPinException;
import de.hs_mannheim.informatik.ct.controller.rest.InvalidPinRestException;
import de.hs_mannheim.informatik.ct.controller.rest.RestErrorMessage;
import de.hs_mannheim.informatik.ct.controller.rest.RoomNotFoundRestException;
import de.hs_mannheim.informatik.ct.persistence.EventNotFoundException;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.RoomFullException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ErrorController {
    
    @ExceptionHandler(value = {RoomNotFoundRestException.class})
    public ResponseEntity<RestErrorMessage> handleRoomNotFoundRestException() {
        RestErrorMessage rem = new RestErrorMessage(404, new Date(), "Room not found.");
        
        return new ResponseEntity<RestErrorMessage>(rem, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(value = {InvalidPinRestException.class})
    public ResponseEntity<RestErrorMessage> handleRoomPinException() {
        RestErrorMessage rem = new RestErrorMessage(400, new Date(), "Room Pin not valid or not submitted.");
        
        return new ResponseEntity<RestErrorMessage>(rem, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({RoomController.RoomNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Room not found")
    public String handleRoomNotFoundException(Model model) {
        model.addAttribute("errorMessage", "Raum nicht gefunden.");
        return "error/errorTemplate";
    }

    @ExceptionHandler({RoomController.InvalidFileUploadException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid file upload")
    public String handleInvalidFileUploadException(Model model) {
        model.addAttribute("errorMessage", "Ungültiger Datei-Upload.");
        return "error/errorTemplate";
    }

    @ExceptionHandler({RoomController.VisitorNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "visitor not found")
    public String handleVisitorNotFoundException(Model model) {
        model.addAttribute("errorMessage", "Besucher nicht gefunden.");

        return "error/errorTemplate";
    }

    @ExceptionHandler({InvalidEmailException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid email")
    public String handleInvalidEmail(Model model) {
        model.addAttribute("errorMessage", "Ungültige Email.");

        return "error/errorTemplate";
    }

    @ExceptionHandler({EventNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "event not found")
    public String handleEventNotFound(Model model) {
        model.addAttribute("errorMessage", "Ereignis nicht gefunden.");

        return "error/errorTemplate";
    }

    @ExceptionHandler({UnsupportedEncodingException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "unsupported encoding")
    public String handleUnsupportedEncodingException(Model model) {
        model.addAttribute("errorMessage", "Codierung wird nicht unterstützt.");

        return "error/errorTemplate";
    }

    @ExceptionHandler({RoomFullException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "room full")
    public String handleRoomFullException(Model model) {
        model.addAttribute("errorMessage", "Raum ist voll.");

        return "error/errorTemplate";
    }

    @ExceptionHandler({InvalidExternalUserdataException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid external userdata")
    public String handleInvalidExternalUserdataException(Model model) {
        model.addAttribute("errorMessage", "Ungültige Benutzerdaten.");

        return "error/errorTemplate";
    }

    @ExceptionHandler({InvalidRoomPinException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid room pin")
    public String handleInvalidRoomPinException(Model model) {
        model.addAttribute("errorMessage", "Ungültige Raum-Pin.");

        return "error/errorTemplate";
    }

    @RequestMapping("/error")
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        log.error("Request Status: {}", status);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }

        }
        return "error";
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "unknown error")
    public String anyException(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log.error(sw.toString()); 

        return "error";
    }

}