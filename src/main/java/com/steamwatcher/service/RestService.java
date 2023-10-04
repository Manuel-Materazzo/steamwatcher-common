package com.steamwatcher.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.steamwatcher.exceptions.RestException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Scope("singleton")
public class RestService {

    private final GoogleService googleService;
    private static final OkHttpClient CLIENT = new OkHttpClient().newBuilder().build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public RestService(GoogleService googleService) {
        this.googleService = googleService;
        MAPPER.registerModule(new JavaTimeModule());
    }

    public synchronized Response executeRequest(Request request) throws IOException {
        return CLIENT.newCall(request).execute();
    }

    public synchronized Response executeM2MRequest(Request request) throws IOException {
        Request authenticatedRequest = request.newBuilder()
                .addHeader("Authorization", "Bearer " + googleService.getToken())
                .build();
        return CLIENT.newCall(authenticatedRequest).execute();
    }

    /**
     * Valida la risposta di paypal (validità uri, codice risposta e presenza del body)
     *
     * @param request  la richiesta
     * @param response la risposta
     */
    public void validateResponse(HttpRequest request, HttpResponse<String> response) throws RestException {
        // Verifica che l'uri della risposta sia uguale a quello della richiesta
        if (response.uri() != request.uri()) {
            String message = "Il server remoto ha risposto da un'uri diverso da quello previsto";
            throw new RestException(message);
        }

        // Verifica che lo status code della risposta sia 200 o 201
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            String message = String.format("Il server remoto ha risposto con un codice di errore %s : %s", response.statusCode(), response.body());
            throw new RestException(message);
        }

        // Verifica che la risposta non sia vuota
        String responseString = response.body();
        if (responseString == null || responseString.isBlank()) {
            String message = "Il server remoto ha risposto con successo, ma ritornando un body vuoto";
            throw new RestException(message);
        }
    }

    /**
     * Valida la risposta di okhttp (validità uri, codice risposta e presenza del body)
     *
     * @param response la risposta
     */
    public String validateResponse(Response response) throws RestException {

        // Verifica che lo status code della risposta sia 200 o 201
        if (!response.isSuccessful()) {
            String message = String.format("Il server remoto ha risposto con un codice di errore %s : %s", response.code(), response.body());
            throw new RestException(message);
        }

        // Verifica che la risposta non sia vuota
        ResponseBody responseBody = response.body();

        String bodyString = "";
        // controllo se c'è il body
        try {
            if (responseBody == null) {
                String message = "Il server remoto ha risposto con successo, ma ritornando un body vuoto";
                throw new RestException(message);
            }
            bodyString = responseBody.string();
            if (bodyString.isBlank()) {
                String message = "Il server remoto ha risposto con successo, ma ritornando un body vuoto";
                throw new RestException(message);
            }
        } catch (IOException e) {
            throw new RestException(e.getMessage());
        } finally {
            response.close();
        }

        return bodyString;
    }

    /**
     * Serializza l'oggetto passato e gestisce gli errori di serializzazione
     *
     * @param object l'oggetto da serializzare
     * @return l'oggetto serializzato
     */
    public String serialize(Object object) throws RestException {
        String serialized;
        try {
            serialized = MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            String message = String.format("Errore nel parsing del body della richiesta: %s", e.getMessage());
            throw new RestException(message);
        }
        return serialized;
    }

    /**
     * Deserializza la risposta http passata e gestisce gli errori di deserializzazione
     *
     * @param response  la risposta http da deserializzare
     * @param valueType la classe in cui deserializzare la risposta
     * @return l'oggetto deserializzato
     */
    public <T> T deserialize(String response, Class<T> valueType) throws RestException {
        try {
            return MAPPER.readValue(response, valueType);
        } catch (Exception e) {
            String message = String.format("Errore nel parsing del body della risposta: %s", e.getMessage());
            throw new RestException(message);
        }
    }

}
