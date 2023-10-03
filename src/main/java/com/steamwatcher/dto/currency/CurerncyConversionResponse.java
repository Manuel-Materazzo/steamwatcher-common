package com.steamwatcher.dto.currency;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurerncyConversionResponse {
    private boolean success;
    private int timestamp;
    private String base;
    private String date;
    private Rates rates;
}
