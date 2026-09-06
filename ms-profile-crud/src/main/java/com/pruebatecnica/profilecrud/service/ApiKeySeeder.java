package com.pruebatecnica.profilecrud.service;

import com.pruebatecnica.profilecrud.entity.ApiKeyEntry;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Al arrancar, garantiza que exista el par de llaves que va a usar
 * ms-profile-query para llamar al endpoint interno. Si ya existe, no hace nada.
 */
@ApplicationScoped
public class ApiKeySeeder {

    private static final Logger LOG = Logger.getLogger(ApiKeySeeder.class);

    @ConfigProperty(name = "internal.client.id")
    String seedClientId;

    @ConfigProperty(name = "internal.client.secret")
    String seedSecret;

    void onStart(@Observes StartupEvent event) {
        if (ApiKeyEntry.find("clientId", seedClientId).firstResultOptional().isEmpty()) {
            ApiKeyEntry entry = new ApiKeyEntry();
            entry.clientId = seedClientId;
            entry.secretHash = BcryptUtil.bcryptHash(seedSecret);
            entry.description = "seeded pair for ms-profile-query internal calls";
            entry.persist();
            LOG.infof("Seeded api key pair for clientId=%s", seedClientId);
        }
    }
}