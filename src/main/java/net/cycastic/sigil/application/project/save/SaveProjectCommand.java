package net.cycastic.sigil.application.project.save;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.IdDto;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveProjectCommand implements Command<IdDto> {
    private @Nullable Integer id;
    private @Nullable Integer userId;
    private @NotNull String projectName;
    private @Null String corsSettings;
}
