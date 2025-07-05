package net.cycastic.sigil.service;

import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.Null;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.exception.RequestException;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public interface LoggedUserAccessor {
    Optional<Integer> tryGetProjectId();

    Optional<Integer> tryGetUserId();

    @Null Claims getClaims();

    boolean hasInvalidClaims();

    Set<String> getRoles();

    String getRequestPath();

    @Nullable Locale getRequestLocale();

    default boolean isAdmin(){
        return getRoles().contains(ApplicationConstants.Roles.ADMIN);
    }

    default int getProjectId(){
        return tryGetProjectId()
                .orElseThrow(() -> new RequestException(400, "Invalid project ID or not exists"));
    }

    default int getUserId(){
        return tryGetUserId()
                .orElseThrow(() -> new RequestException(401, "User not signed in"));
    }
}
