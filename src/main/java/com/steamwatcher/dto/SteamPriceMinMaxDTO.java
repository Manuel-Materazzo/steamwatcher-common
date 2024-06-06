package com.steamwatcher.dto;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SteamPriceMinMaxDTO implements Serializable {
    private Instant date;
    private double minAmount;
    private double maxAmount;
    private double lastAmount;
    private int volumes;
}
