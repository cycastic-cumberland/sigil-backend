package net.cycastic.portfoliotoolkit.application.email.preview;

import an.awesome.pipelinr.Command;
import lombok.*;
import net.cycastic.portfoliotoolkit.configuration.EmailTemplateConfigurations;
import net.cycastic.portfoliotoolkit.domain.ApplicationUtilities;
import net.cycastic.portfoliotoolkit.domain.exception.RequestException;
import net.cycastic.portfoliotoolkit.domain.repository.listing.AttachmentListingRepository;
import net.cycastic.portfoliotoolkit.service.EmailTemplateEngine;
import net.cycastic.portfoliotoolkit.service.LoggedUserAccessor;
import net.cycastic.portfoliotoolkit.service.StorageProvider;
import net.cycastic.portfoliotoolkit.service.impl.EphemeralInputStream;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.*;
import java.util.Stack;

@Component
@RequiredArgsConstructor
public class PreviewEmailCommandHandler implements Command.Handler<PreviewEmailCommand, InputStreamSource> {
    private final EmailTemplateEngine emailTemplateEngine;
    private final AttachmentListingRepository attachmentListingRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final StorageProvider storageProvider;
    private final EmailTemplateConfigurations emailTemplateConfigurations;

    @SneakyThrows
    private InputStream render(PreviewEmailCommand command){
        var projectId = loggedUserAccessor.getProjectId();
        var listing = attachmentListingRepository.findByListing_Project_IdAndListing_ListingPath(projectId, command.getTemplatePath())
                .orElseThrow(() -> new RequestException(404, "Listing does not exists"));
        if (!listing.getObjectKey().endsWith(".ftl") && !listing.getMimeType().equals("text/x-freemarker") && !listing.getMimeType().equals("application/x-freemarker")){
            throw new RequestException(400, "Supplied file is not a valid FreeMarker template");
        }
        if (emailTemplateConfigurations.getMaxTemplateFileSize() != null){
            long limit = emailTemplateConfigurations.getMaxTemplateFileSize();
            var fileSize = storageProvider.getBucket(listing.getBucketName()).getObjectSize(listing.getObjectKey());
            if (fileSize > limit){
                throw new RequestException(413, "Template file limit exceeded");
            }
        }

        var templatePath = ApplicationUtilities.getTempFile();
        var tempFiles = new Stack<Path>();
        tempFiles.push(templatePath);
        try {
            try (var template = Files.newOutputStream(templatePath, StandardOpenOption.CREATE)){
                storageProvider.getBucket(listing.getBucketName())
                        .downloadFile(listing.getObjectKey(), template);
            }
            if (emailTemplateConfigurations.getMaxTemplateFileSize() != null){
                long limit = emailTemplateConfigurations.getMaxTemplateFileSize();
                var fileSize = Files.size(templatePath);
                if (fileSize > limit){
                    throw new RequestException(413, "Template file limit exceeded");
                }
            }

            var renderedTemplatePath = ApplicationUtilities.getTempFile();
            tempFiles.push(renderedTemplatePath);
            try (var template = Files.newInputStream(templatePath, StandardOpenOption.READ)){
                try (var render = Files.newOutputStream(renderedTemplatePath, StandardOpenOption.CREATE)){
                    emailTemplateEngine.render(template, render, command.getConstants());
                }
            }

            Files.delete(templatePath);
            return new EphemeralInputStream(renderedTemplatePath);
        } catch (Exception e){
            while (!tempFiles.isEmpty()){
                var file = tempFiles.pop();
                try {
                    Files.delete(file);
                } catch (NoSuchFileException ex){
                    // ignored
                }
            }
            throw e;
        }
    }

    @Override
    @SneakyThrows
    public InputStreamSource handle(PreviewEmailCommand command) {
        return () -> render(command);
    }
}
