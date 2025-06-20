package net.cycastic.portfoliotoolkit.application.listing.query;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.dto.listing.ListingDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageRequestDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageResponseDto;
import org.springframework.lang.Nullable;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QueryListingCommand extends PageRequestDto implements Command<PageResponseDto<ListingDto>> {
    private @Nullable Integer projectId;

    private String prefix;
}
