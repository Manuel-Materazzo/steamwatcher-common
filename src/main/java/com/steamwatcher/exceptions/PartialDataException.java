package com.steamwatcher.exceptions;

import com.steamwatcher.dto.SteamPriceMinMaxDTO;
import org.springframework.http.HttpStatus;

import java.util.List;

public class PartialDataException extends RuntimeException  {

    private HttpStatus httpStatus = HttpStatus.PARTIAL_CONTENT;

    private List<SteamPriceMinMaxDTO> innerContent;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public List<SteamPriceMinMaxDTO> getContent() {
        return innerContent;
    }

    public PartialDataException(String message, List<SteamPriceMinMaxDTO> content) {
        super(message);
        this.innerContent = content;
    }


}
