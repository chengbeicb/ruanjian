package com.shop.exception;

import java.util.Date;

public class ErrorDetails {
    private int statusCode;
    private String message;
    private String details;
    private Date timestamp;
    
    public ErrorDetails(int statusCode, String message, String details) {
        this.timestamp = new Date();
        this.statusCode = statusCode;
        this.message = message;
        this.details = details;
    }
    
    // Getters and Setters
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}