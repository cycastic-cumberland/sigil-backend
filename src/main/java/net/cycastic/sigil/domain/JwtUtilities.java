package net.cycastic.sigil.domain;

import io.jsonwebtoken.Claims;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtUtilities {
    public static Collection<GrantedAuthority> extractAuthorities(@NonNull Claims claims){
        var entry = claims.get(ApplicationConstants.ROLES_ENTRY);
        if (!(entry instanceof Collection<?> roles)){
            return HashSet.newHashSet(0);
        }
        return roles.stream()
                .map(r -> (GrantedAuthority)new SimpleGrantedAuthority(r.toString()))
                .toList();
    }

    public static Set<String> extractRoles(@NonNull Claims claims){
        var entry = claims.get(ApplicationConstants.ROLES_ENTRY);
        if (!(entry instanceof Collection<?> roles)){
            return HashSet.newHashSet(0);
        }
        return roles.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
