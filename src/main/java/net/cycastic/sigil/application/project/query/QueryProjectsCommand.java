package net.cycastic.sigil.application.project.query;

import an.awesome.pipelinr.Command;
import lombok.*;
import net.cycastic.sigil.domain.dto.ProjectDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QueryProjectsCommand extends PageRequestDto implements Command<PageResponseDto<ProjectDto>> {
    private Integer userId;
}
