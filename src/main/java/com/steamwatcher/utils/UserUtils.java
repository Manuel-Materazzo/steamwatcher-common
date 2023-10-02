package com.steamwatcher.utils;

import com.steamwatcher.dto.TokenPayload;
import org.modelmapper.ModelMapper;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class UserUtils {

    private UserUtils() {
    }

    private static final ModelMapper MAPPER = new ModelMapper();

    public static String getGoogleIdFromToken(JwtAuthenticationToken authToken) {

        TokenPayload tp = MAPPER.map(authToken.getTokenAttributes(), TokenPayload.class);

        return tp.getSub();
    }
}
