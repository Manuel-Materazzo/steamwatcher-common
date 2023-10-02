package com.steamwatcher.dto;

import lombok.*;

import java.util.ArrayList;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class TokenPayload {
    private String at_hash;
    private String sub;
    private boolean email_verified;
    private String iss;
    private String given_name;
    private String locale;
    private String picture;
    ArrayList<String> aud = new ArrayList <> ();
    private String azp;
    private String name;
    private String hd;
    private String exp;
    private String family_name;
    private String iat;
    private String email;
    private String jti;
}
