package com.steamwatcher.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ExceptionHandlerAdvice {

    Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(value = { GeneralDataException.class })
    public ResponseEntity<Object> handleGeneralException(GeneralDataException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorMessage(e.getMessage()));
    }

    @ExceptionHandler(value = { HttpMessageNotWritableException.class })
    public ResponseEntity<Object> handleStrangeException(HttpMessageNotWritableException e) {
        logger.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(new ErrorMessage(e.getMessage()));
    }

    // TODO: far funzionare l'entrypoint nel security config
    @ExceptionHandler(value = { AccessDeniedException.class })
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> handleException(Exception e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(e.getMessage()));
    }

}

