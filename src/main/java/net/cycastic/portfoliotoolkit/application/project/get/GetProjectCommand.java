package net.cycastic.portfoliotoolkit.application.project.get;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.dto.ProjectDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetProjectCommand implements Command<ProjectDto> {
    private int projectId;
}
