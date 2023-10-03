package com.steamwatcher.dto;

import lombok.*;

import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SteamPriceMinMaxDTO {
    private Instant date;
    private double minAmount;
    private double maxAmount;
    private double lastAmount;
    private int volumes;
}
