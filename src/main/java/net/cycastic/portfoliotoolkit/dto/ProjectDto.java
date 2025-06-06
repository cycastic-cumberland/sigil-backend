package net.cycastic.portfoliotoolkit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.cycastic.portfoliotoolkit.domain.model.Project;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    @NotNull
    private Integer id;

    @NotNull
    private String projectName;

    @Null
    private String corsSettings;

    @NotNull
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    public static ProjectDto fromDomain(Project project){
        return ProjectDto.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .corsSettings(project.getCorsSettings())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .removedAt(project.getRemovedAt())
                .build();
    }
}
