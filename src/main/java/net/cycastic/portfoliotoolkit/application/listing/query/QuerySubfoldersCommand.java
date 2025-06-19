package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.dto.SubfoldersDto;

@Data
public class QuerySubfoldersCommand implements Command<SubfoldersDto> {
    private @NotNull String folder;
}
