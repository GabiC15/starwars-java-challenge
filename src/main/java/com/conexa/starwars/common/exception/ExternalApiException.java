package com.conexa.starwars.common.exception;

// thrown when SWAPI is down, times out, or sends back garbage - maps to 502 instead of 404
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
