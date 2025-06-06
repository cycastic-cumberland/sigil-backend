package net.cycastic.portfoliotoolkit.application.listing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.NsoUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.ForbiddenException;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.model.Listing;
import net.cycastic.portfoliotoolkit.domain.model.ListingAccessControlPolicy;
import net.cycastic.portfoliotoolkit.domain.repository.ListingACPRepository;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class ListingACPService {
    private final ListingACPRepository listingACPRepository;
    private final UserRepository userRepository;
    private final LoggedUserAccessor loggedUserAccessor;

    public List<ListingAccessControlPolicy> getOrderedPolicies(@NonNull Listing listing){
        var project = listing.getProject();
        return listingACPRepository.findListingAccessControlPoliciesByProject(project,
                Sort.by("priority").ascending());
    }

    public void verifyAccess(@NonNull Listing listing){
        if (loggedUserAccessor.isAdmin()){
            return;
        }

        var project = listing.getProject();
        if (project.getUser().getId().equals(loggedUserAccessor.getUserId())){
            return;
        }
        var currentUser = userRepository.findById(loggedUserAccessor.getUserId())
                .orElseThrow(() -> new RequestException(404, "Could not found user"));
        var policies = listingACPRepository.findListingAccessControlPoliciesByProject(project,
                Sort.by("priority").ascending());
        for (var policy : policies){
            if (!(policy.getApplyTo() == null || policy.getApplyTo().getId().equals(currentUser.getId()))){
                continue;
            }

            // lowSearchKey <= listing.searchKey < highSearchKey
            if (policy.getLowSearchKey() != null){
                if (!(NsoUtilities.compareByteArrays(listing.getSearchKey(), policy.getLowSearchKey()) >= 0)){
                    continue;
                }
            }
            if (policy.getHighSearchKey() != null){
                if (!(NsoUtilities.compareByteArrays(listing.getSearchKey(), policy.getHighSearchKey()) < 0)){
                    continue;
                }
            }

            if (policy.isAllowed()){
                // Returns to allow
                return;
            }

            // Breaks to throw
            break;
        }

        // Forbids if exhaust all policies
        throw new ForbiddenException();
    }
}
