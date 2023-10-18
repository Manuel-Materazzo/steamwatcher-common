package com.steamwatcher.service;

import com.steamwatcher.dto.currency.CurerncyConversionResponse;
import com.steamwatcher.dto.currency.Rates;
import com.steamwatcher.exceptions.RestException;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CurrencyConversionService {

    private final RestService restService;
    private Rates conversionRates;
    private Instant retrivalTime = Instant.now().minus(7, ChronoUnit.DAYS);

    @Value("${fixer.api-key:}")
    private String fixerApiKey;

    Logger logger = LoggerFactory.getLogger(CurrencyConversionService.class);


    @Autowired
    CurrencyConversionService(RestService restService) {
        this.restService = restService;
    }

    public BigDecimal cnyToEur(BigDecimal cny) {

        refreshRatesIfNeeded();

        BigDecimal eurToCnyRate = conversionRates.getCny();

        return cny.divide(eurToCnyRate, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal cnyToUsd(BigDecimal cny) {

        refreshRatesIfNeeded();

        BigDecimal eurToCnyRate = conversionRates.getCny();
        BigDecimal eurToUsdRate = conversionRates.getUsd();

        BigDecimal eur = cny.divide(eurToCnyRate, 2, RoundingMode.HALF_UP);

        return eur.multiply(eurToUsdRate);
    }

    public BigDecimal usdToEur(BigDecimal usd) {

        refreshRatesIfNeeded();

        BigDecimal eurToUsdRate = conversionRates.getUsd();

        BigDecimal eur = usd.divide(eurToUsdRate, 2, RoundingMode.HALF_UP);

        return eur.multiply(eurToUsdRate);
    }


    private void refreshRatesIfNeeded() {
        // if there is a conversion rate and it's newer than 1 hour, it's fine
        if (conversionRates != null ||
                retrivalTime.isAfter(Instant.now().minus(6, ChronoUnit.HOURS))) {
            return;
        }

        String uri = String.format("http://data.fixer.io/api/latest?access_key=%s&format=1", fixerApiKey);

        Request request = new Request.Builder()
                .url(uri)
                .method("GET", null)
                .build();

        try (Response response = restService.executeRequest(request)) {

            String bodyString = restService.validateResponse(response);

            CurerncyConversionResponse res = restService.deserialize(bodyString, CurerncyConversionResponse.class);

            conversionRates = res.getRates();
            retrivalTime = Instant.now();

        } catch (IOException | RestException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

}
