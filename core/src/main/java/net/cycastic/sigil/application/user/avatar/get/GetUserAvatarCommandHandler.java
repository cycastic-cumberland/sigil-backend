package net.cycastic.sigil.application.user.avatar.get;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import net.cycastic.sigil.service.InputStreamResponse;
import net.cycastic.sigil.service.impl.GravatarService;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GetUserAvatarCommandHandler implements Command.Handler<GetUserAvatarCommand, InputStreamResponse> {
    private final GravatarService gravatarService;
    private final UserRepository userRepository;

    @Override
    public InputStreamResponse handle(GetUserAvatarCommand command) {
        var user = userRepository.findByAvatarToken(command.getAvatarToken())
                .orElseThrow(() -> new RequestException(404, "User not found"));
        final var bytes = gravatarService.getAvatar(user.getEmail(), Objects.requireNonNullElse(command.getSize(), 200));
        return new InputStreamResponse() {
            @Override
            public Long getContentLength() {
                return (long) bytes.length;
            }

            @Null
            @Override
            public String getFileName() {
                return null;
            }

            @Override
            public String getMimeType() {
                return "image/png";
            }

            @Override
            public @NonNull InputStream getInputStream() {
                return new ByteArrayInputStream(bytes);
            }
        };
    }
}
