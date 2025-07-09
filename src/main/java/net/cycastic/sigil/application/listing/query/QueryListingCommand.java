package net.cycastic.sigil.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.domain.dto.listing.ListingDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QueryListingCommand extends PageRequestDto implements Command<PageResponseDto<ListingDto>> {
    private String prefix;
}
