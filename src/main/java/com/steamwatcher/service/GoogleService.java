package com.steamwatcher.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.steamwatcher.utils.FileReaderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Scope("singleton")
public class GoogleService {

    private static Credential CREDENTIALS;

    @Value("${csgotracker.m2m-auth-file:}")
    private String authFilename;

    public String getToken() throws IOException {

        // se non ho credenziali loggo
        if (CREDENTIALS == null) {
            initializeCredentials();
        }

        // se il login è scaduto refresho
        if (CREDENTIALS.getExpiresInSeconds() < 0) {
            boolean result = CREDENTIALS.refreshToken();
            // se non riesco a refreshare, riloggo
            if (!result) {
                initializeCredentials();
            }
        }

        return CREDENTIALS.getAccessToken();
    }

    private void initializeCredentials() throws IOException {
        // leggo il file
        InputStream in = FileReaderUtils.getInputstream(authFilename);

        // estraggo la lista di scope per gli spreadsheet
        List<String> scopes = List.of("");
        // genero le credenziali
        CREDENTIALS = GoogleCredential.fromStream(in).createScoped(scopes);

    }
}