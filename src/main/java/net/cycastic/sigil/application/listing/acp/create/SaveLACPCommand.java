package net.cycastic.sigil.application.listing.acp.create;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.listing.SaveListingACPDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveLACPCommand implements Command<@Null Object> {
    private List<SaveListingACPDto> policies;
}
