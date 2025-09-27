package net.cycastic.sigil.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.sigil.domain.ApplicationUtilities;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.StoragePresigner;
import net.cycastic.sigil.service.UrlAccessor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class StoragePresignerImpl implements StoragePresigner {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UrlAccessor urlAccessor;
    private final UriPresigner presigner;

    @Override
    @SneakyThrows
    public String sign(String listingPath) {
        var projectId = loggedUserAccessor.getTenantId();
        var userId = loggedUserAccessor.getUserId();
        var url = new StringBuilder(urlAccessor.getBackendOrigin())
                .append("/api/storage")
                .append("?projectId=").append(projectId)
                .append("&userId=").append(userId)
                .append("&path=").append(ApplicationUtilities.encodeURIComponent(listingPath))
                .toString();
        return presigner.signUri(URI.create(url)).toString();
    }
}
