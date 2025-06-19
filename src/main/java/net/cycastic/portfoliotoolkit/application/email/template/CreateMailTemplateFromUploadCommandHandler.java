package net.cycastic.portfoliotoolkit.application.email.template;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.service.ListingService;
import net.cycastic.portfoliotoolkit.domain.repository.ProjectRepository;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.domain.dto.AttachmentPresignedUploadDto;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateMailTemplateFromUploadCommandHandler implements Command.Handler<CreateMailTemplateFromUploadCommand, AttachmentPresignedUploadDto> {
    private final ListingService listingService;
    private final StorageProvider storageProvider;
    private final ProjectRepository projectRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final AttachmentListingRepository attachmentListingRepository;

    @Override
    public AttachmentPresignedUploadDto handle(CreateMailTemplateFromUploadCommand command) {
        throw new UnsupportedOperationException();
    }
}
