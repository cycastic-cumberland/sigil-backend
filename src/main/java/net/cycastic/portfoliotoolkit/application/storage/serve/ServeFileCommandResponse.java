package net.cycastic.portfoliotoolkit.application.storage.serve;

import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.InputStreamSource;
import org.springframework.lang.Nullable;

public record ServeFileCommandResponse(InputStreamSource streamSource, @NotNull String fileName, @Nullable String mimeType) {
}
