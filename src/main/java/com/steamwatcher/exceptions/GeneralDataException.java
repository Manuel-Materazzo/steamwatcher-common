package com.steamwatcher.exceptions;

import org.springframework.http.HttpStatus;

public class GeneralDataException extends RuntimeException  {

    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    public static final String UNABLE_TO_RETRIEVE_RECORD = "Impossibile recuperare i record";
    public static final String UNABLE_TO_UPDATE_RECORD = "Impossibile aggiornare il record";
    public static final String UNABLE_TO_DELETE_RECORD = "Impossibile eliminare il record";
    public static final String UNABLE_TO_CREATE_RECORD = "Impossibile creare il record";
    public static final String ONLY_M2M = "Allowed only with M2M comunication";

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public GeneralDataException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public GeneralDataException(String message) {
        super(message);
    }


}
