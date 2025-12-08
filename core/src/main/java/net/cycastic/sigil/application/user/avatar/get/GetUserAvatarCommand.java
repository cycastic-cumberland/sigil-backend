package net.cycastic.sigil.application.user.avatar.get;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.sigil.service.InputStreamResponse;

import java.util.UUID;

@Data
public class GetUserAvatarCommand implements Command<InputStreamResponse> {
    @NotNull
    private UUID avatarToken;

    @Nullable
    @Min(100)
    @Max(500)
    private Integer size;
}
