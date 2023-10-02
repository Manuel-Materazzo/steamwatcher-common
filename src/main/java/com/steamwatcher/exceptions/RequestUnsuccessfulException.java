package com.steamwatcher.exceptions;

public class RequestUnsuccessfulException extends RuntimeException {

    public RequestUnsuccessfulException(String message){
        super(message);
    }
}
