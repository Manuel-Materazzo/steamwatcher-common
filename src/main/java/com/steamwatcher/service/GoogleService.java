package com.steamwatcher.service;

import com.google.auth.oauth2.IdToken;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.steamwatcher.utils.FileReaderUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Scope("singleton")
public class GoogleService {

    private ServiceAccountCredentials credentials;

    @Value("${steamwatcher.m2m-auth-file:}")
    public String authFilename;

    public String getToken() throws IOException {

        // se non ho credenziali loggo
        if (credentials == null) {
            initializeCredentials();
        }

        // se il login è scaduto refresho
        credentials.refreshIfExpired();

        // estraggo l'id token
        String audience = "https://steamwatcher.com";
        IdToken idToken = credentials.idTokenWithAudience(audience, new ArrayList<>());

        return idToken.getTokenValue();
    }

    private void initializeCredentials() throws IOException {
        // leggo il file
        InputStream in = FileReaderUtils.getInputstream(authFilename);

        // estraggo la lista di scope per gli spreadsheet
        List<String> scopes = List.of("openid", "profile", "email");
        // genero le credenziali
        credentials = (ServiceAccountCredentials) ServiceAccountCredentials.fromStream(in).createScoped(scopes);
    }
}