package de.hs_mannheim.informatik.ct.controller;

import de.hs_mannheim.informatik.ct.persistence.EventNotFoundException;
import de.hs_mannheim.informatik.ct.persistence.InvalidEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ErrorController {
    @ExceptionHandler({RoomController.RoomNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleRoomNotFoundException(RoomController.RoomNotFoundException roomNotFound, WebRequest request) {
        return "error/roomNotFound";
    }

    @ExceptionHandler({RoomController.InvalidFileUploadException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidFileUploadException(RoomController.InvalidFileUploadException roomNotFound, WebRequest request) {
        return "error/invalidFileUpload";
    }

    @ExceptionHandler({RoomController.VisitorNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleVisitorNotFoundException(RoomController.VisitorNotFoundException roomNotFound, WebRequest request) {
        return "error/visitorNotFound";
    }

    @ExceptionHandler({InvalidEmailException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidEmail(InvalidEmailException invalidEmail, WebRequest request) {
        return "error/invalidEmail";
    }

    @ExceptionHandler({EventNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEventNotFound(EventNotFoundException eventNotFound, WebRequest request) {
        return "error/eventNotFound";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }

        }
        return "error/error";
    }

    @ExceptionHandler({Exception.class})
    public String anyException() {
        return "index";
    }

}
