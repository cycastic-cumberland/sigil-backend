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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Lazy
@Service
@RequiredArgsConstructor
public class ListingService {
    private static final Pattern SPECIAL_CHARACTERS_ESCAPE_PATTERN = Pattern.compile("[\\\\%_]");
    private static final Pattern SPECIAL_CHARACTERS_UNESCAPE_PATTERN = Pattern.compile("\\\\\\\\|\\\\%|\\\\_");

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
            if (!(policy.getApplyToId() == null || policy.getApplyToId().equals(currentUser.getId()))){
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

    public static byte[] encodeSearchKey(@NonNull String path){
        var matcher = SPECIAL_CHARACTERS_ESCAPE_PATTERN.matcher(path);
        var result = matcher.replaceAll(match -> switch (match.group()) {
            case "\\" -> "\\\\\\\\";
            case "%" -> "\\\\%";
            case "_" -> "\\\\_";
            default -> match.group();
        });
        var parts = result.split("/");
        var buffer = ByteBuffer.allocate(NsoUtilities.KEY_LENGTH * NsoUtilities.SEPARATOR_SEQUENCE_LENGTH);

        NsoUtilities.insertSeparator(buffer);
        var first = true;
        for (var part : parts){
            if (part.isEmpty()){
                continue;
            }
            if (first){
                first = false;
            } else {
                NsoUtilities.insertSeparator(buffer);
            }
            buffer.put(part.getBytes(StandardCharsets.UTF_8));
        }

        var finalArray = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, finalArray, 0, buffer.position());
        return finalArray;
    }

    public static String decodeSearchKey(byte @NonNull [] searchKey){
        var sb = new StringBuilder();
        var delimeterAsString = NsoUtilities.delimeterAsString();
        for (var part : NsoUtilities.split(searchKey)){
            if (part.length == 0){
                continue;
            }

            var matcher = SPECIAL_CHARACTERS_UNESCAPE_PATTERN.matcher(new String(part, StandardCharsets.UTF_8));
            var result = matcher.replaceAll(match -> switch (match.group()) {
                case "\\\\\\\\" -> "\\";
                case "\\\\%" -> "%";
                case "\\\\_" -> "_";
                default -> match.group().equals(delimeterAsString) ? "" : match.group();
            });
            sb.append('/').append(result);
        }

        return sb.toString();
    }
}
