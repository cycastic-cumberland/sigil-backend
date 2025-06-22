package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.project.delete.DeleteProjectCommand;
import net.cycastic.portfoliotoolkit.application.project.get.GetProjectCommand;
import net.cycastic.portfoliotoolkit.application.project.query.QueryProjectsCommand;
import net.cycastic.portfoliotoolkit.application.project.save.SaveProjectCommand;
import net.cycastic.portfoliotoolkit.domain.dto.IdDto;
import net.cycastic.portfoliotoolkit.domain.dto.ProjectDto;
import net.cycastic.portfoliotoolkit.domain.dto.paging.PageResponseDto;
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
