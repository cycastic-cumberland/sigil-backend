package net.cycastic.portfoliotoolkit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StoragePresigner;
import net.cycastic.portfoliotoolkit.service.UrlAccessor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class StoragePresignerImpl implements StoragePresigner {
    private final AttachmentListingRepository attachmentListingRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final UrlAccessor urlAccessor;
    private final UriPresigner presigner;

    @Override
    @SneakyThrows
    public String sign(String listingPath) {
        var projectId = loggedUserAccessor.getProjectId();
        var userId = loggedUserAccessor.getUserId();
        var listing = attachmentListingRepository.findByListing_Project_IdAndListing_ListingPath(projectId, listingPath)
                .orElseThrow(() -> new RequestException(404, "Listing does not exists"));
        var token = listing.getShareToken();
        var url = new StringBuilder(urlAccessor.getBackendOrigin())
                .append("/api/storage")
                .append("?projectId=").append(projectId)
                .append("&userId=").append(userId)
                .append("&shareToken=").append(token)
                .append("&path=").append(ApplicationUtilities.encodeURIComponent(listingPath))
                .toString();
        return presigner.signUri(URI.create(url)).toString();
    }
}
