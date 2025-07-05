package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.project.delete.DeleteProjectCommand;
import net.cycastic.sigil.application.project.get.GetProjectCommand;
import net.cycastic.sigil.application.project.query.QueryProjectsCommand;
import net.cycastic.sigil.application.project.save.SaveProjectCommand;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.ProjectDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/projects")
public class ProjectsController {
    private final Pipelinr pipelinr;

    @GetMapping("project")
    public ProjectDto getProject(GetProjectCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("project")
    public IdDto saveProject(@RequestBody SaveProjectCommand command){
        return pipelinr.send(command);
    }

    @DeleteMapping("project")
    public void deleteProject(DeleteProjectCommand command){
        pipelinr.send(command);
    }

    @GetMapping
    public PageResponseDto<ProjectDto> getProjects(QueryProjectsCommand command){
        return pipelinr.send(command);
    }
}
