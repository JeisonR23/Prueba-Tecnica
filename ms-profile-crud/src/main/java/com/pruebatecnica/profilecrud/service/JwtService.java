package com.pruebatecnica.profilecrud.service;

import java.time.Duration;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JwtService {

    public String issueToken(String subject) {

        return Jwt.issuer("ms-profile-crud")
                .subject(subject)
                .groups("profile-owner")
                .expiresIn(Duration.ofHours(2))
                .sign();
    }
    
}
