package net.cycastic.portfoliotoolkit.application.listing.attachment;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.attachment.create.CreateAttachmentListingCommand;
import net.cycastic.portfoliotoolkit.application.validation.CommandValidator;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedDto;
import net.cycastic.portfoliotoolkit.domain.dto.listing.AttachmentUploadDto;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.UserRepository;
import net.cycastic.portfoliotoolkit.service.LimitProvider;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AttachmentListingUploadCommandValidator implements CommandValidator {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentListingUploadCommandValidator.class);
    private static final Pattern INVALID_PATH = Pattern.compile("/{2}|\\p{Cntrl}");
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final LimitProvider limitProvider;

    @Override
    public boolean matches(Command o) {
        return o instanceof AttachmentUploadDto;
    }

    @Override
    public void validate(Command o) {
        var command = (AttachmentUploadDto)o;
        var path = command.getPath();
        if (!path.startsWith("/")){
            throw new RequestException(400, "Listing path must start with a forward slash");
        }
        if (path.endsWith("/")){
            throw new RequestException(400, "Listing path must not end with a forward slash");
        }
        if (INVALID_PATH.matcher(path).find()) {
            throw new RequestException(400, "Path must not contain //, _ or %");
        }
        if (command.getContentLength() <= 0){
            throw new RequestException(400, "Invalid content length");
        }

        var user = userRepository.findById(loggedUserAccessor.getUserId())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        var limit = limitProvider.extractUsageDetails(user);
        if (limit.getPerAttachmentSize() != null && command.getContentLength() > limit.getPerAttachmentSize()){
            logger.error("File is larger than permitted limit. Size: {} byte(s), limit: {} byte(s)",
                    command.getContentLength(), limit.getPerAttachmentSize());
            throw new RequestException(413, "File is larger than permitted limit.");
        }

        var acc = command.getContentLength() + user.getAccumulatedAttachmentStorageUsage();
        if (limit.getAllAttachmentSize() != null && acc > limit.getAllAttachmentSize()){
            throw new RequestException(413, "Accumulated storage usage exceeded");
        }
    }
}
