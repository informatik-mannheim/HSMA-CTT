package de.hs_mannheim.informatik.ct.controller.rest;

import java.util.Date;

public class RestErrorMessage {
    private int statusCode;
    private Date timestamp;
    private String message;
    
    public RestErrorMessage(int statusCode, Date timestamp, String message) {
        super();
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.message = message;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public String getMessage() {
        return message;
    }
    
}
