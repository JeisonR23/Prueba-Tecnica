package com.pruebatecnica.profilequery.client;

import com.pruebatecnica.profilequery.dto.ProfileResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ServiceUnavailableException;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;

@RegisterRestClient(configKey = "profile-crud")
@RegisterProvider(BasicAuthClientFilter.class)
@RegisterProvider(ProfileCrudExceptionMapper.class)
public interface ProfileCrudClient {

    @GET
    @Path("/internal/profile/{id}")
    @Timeout(unit = ChronoUnit.MILLIS, value = 2000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, skipOn = NotFoundException.class)
    @Fallback(fallbackMethod = "getProfileUnavailable", skipOn = NotFoundException.class)
    ProfileResponse getProfile(@PathParam("id") String id);

    default ProfileResponse getProfileUnavailable(String id) {
        throw new ServiceUnavailableException("ms-profile-crud no esta disponible ahora mismo, intenta de nuevo en unos segundos");
    }

    @GET
    @Path("/internal/profile/search")
    @Timeout(unit = ChronoUnit.MILLIS, value = 2000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, skipOn = NotFoundException.class)
    @Fallback(fallbackMethod = "searchByEmailUnavailable", skipOn = NotFoundException.class)
    ProfileResponse searchByEmail(@QueryParam("email") String email);

    default ProfileResponse searchByEmailUnavailable(String email) {
        throw new ServiceUnavailableException("ms-profile-crud no esta disponible ahora mismo, intenta de nuevo en unos segundos");
    }
}