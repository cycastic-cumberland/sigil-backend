package net.cycastic.portfoliotoolkit.application.project.query;

import an.awesome.pipelinr.Command;
import lombok.*;
import net.cycastic.portfoliotoolkit.domain.dto.ProjectDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageRequestDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageResponseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class QueryProjectsCommand extends PageRequestDto implements Command<PageResponseDto<ProjectDto>> {
    private Integer userId;
}
