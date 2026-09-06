package com.pruebatecnica.profilecrud.service;

import java.util.Optional;

import com.pruebatecnica.profilecrud.entity.ApiKeyEntry;

import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.elytron.security.common.BcryptUtil;

@ApplicationScoped

public class ApiKeyService {

    public boolean isValidPair(String clientId, String secret) {

        if (clientId == null || secret == null) {
            return false;
        }
        Optional<ApiKeyEntry> entry = ApiKeyEntry.find("clientId", clientId).firstResultOptional();
        return entry.map(apiKeyEntry -> BcryptUtil.matches(secret, apiKeyEntry.secretHash)).orElse(false);
    }
}





