package de.hs_mannheim.informatik.ct.controller.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Room not found")
public class InvalidPinRestException extends RuntimeException {
}
