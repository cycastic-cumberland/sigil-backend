package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.dto.listing.ListingDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageRequestDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageResponseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QueryListingCommand extends PageRequestDto implements Command<PageResponseDto<ListingDto>> {
    private String prefix;
}
