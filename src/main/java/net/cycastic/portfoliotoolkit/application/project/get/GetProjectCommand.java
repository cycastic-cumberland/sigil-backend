package net.cycastic.portfoliotoolkit.application.project.get;

import an.awesome.pipelinr.Command;
import net.cycastic.portfoliotoolkit.dto.ProjectDto;

public record GetProjectCommand(int projectId) implements Command<ProjectDto> {}
