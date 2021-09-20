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

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.hs_mannheim.informatik.ct.controller.exception.InvalidRoomPinException;
import de.hs_mannheim.informatik.ct.persistence.EventNotFoundException;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import de.hs_mannheim.informatik.ct.persistence.InvalidExternalUserdataException;
import de.hs_mannheim.informatik.ct.persistence.RoomFullException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ErrorController {
    @ExceptionHandler({RoomController.RoomNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Room not found")
    public String handleRoomNotFoundException(Model model) {
        model.addAttribute("errorMessage", "Room not found");
        return "error/errorTemplate";
    }

    @ExceptionHandler({RoomController.InvalidFileUploadException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid file upload")
    public String handleInvalidFileUploadException(Model model) {
        model.addAttribute("errorMessage", "File upload is invalid");
        return "error/errorTemplate";
    }

    @ExceptionHandler({RoomController.VisitorNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "visitor not found")
    public String handleVisitorNotFoundException(Model model) {
        model.addAttribute("errorMessage", "Visitor not found");

        return "error/errorTemplate";
    }

    @ExceptionHandler({InvalidEmailException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid email")
    public String handleInvalidEmail(Model model) {
        model.addAttribute("errorMessage", "Email is invalid");

        return "error/errorTemplate";
    }

    @ExceptionHandler({EventNotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "event not found")
    public String handleEventNotFound(Model model) {
        model.addAttribute("errorMessage", "Event not found");

        return "error/errorTemplate";
    }

    @ExceptionHandler({UnsupportedEncodingException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "unsupported encoding")
    public String handleUnsupportedEncodingException(Model model) {
        model.addAttribute("errorMessage", "Encoding is not supported");

        return "error/errorTemplate";
    }

    @ExceptionHandler({RoomFullException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "room full")
    public String handleRoomFullException(Model model) {
        model.addAttribute("errorMessage", "The room is full");

        return "error/errorTemplate";
    }

    @ExceptionHandler({InvalidExternalUserdataException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid external userdata")
    public String handleInvalidExternalUserdataException(Model model) {
        model.addAttribute("errorMessage", "Userdata is invalid");

        return "error/errorTemplate";
    }

    @ExceptionHandler({InvalidRoomPinException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid room pin")
    public String handleInvalidRoomPinException(Model model) {
        model.addAttribute("errorMessage", "Room pin is invalid");

        return "error/errorTemplate";
    }

    @RequestMapping("/error")
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
//    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        System.out.println("Request Status: " + status);

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
