package net.cycastic.portfoliotoolkit.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String projectName;

    @NotNull
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @NotNull
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private OffsetDateTime removedAt;

    public Project(){
        createdAt = OffsetDateTime.now();
    }
}
