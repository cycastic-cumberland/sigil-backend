package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.FolderItemsDto;

@Data
public class QuerySingleLevelCommand implements Command<FolderItemsDto> {
    private @NotNull String folder;
}
