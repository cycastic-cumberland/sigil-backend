package net.cycastic.sigil.application.pm.task;

import net.cycastic.sigil.application.pm.task.status.transit.connect.ConnectTaskStatusCommand;
import net.cycastic.sigil.application.pm.task.status.transit.connect.ConnectTaskStatusCommandHandler;
import net.cycastic.sigil.application.pm.task.status.transit.connect.Connection;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskProgress;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.ProjectPartitionRepository;
import net.cycastic.sigil.domain.repository.pm.TaskProgressRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConnectTaskStatusCommandHandlerTest {
    private static final int PARTITION_ID = 67;

    private TaskStatusRepository taskStatusRepository;
    private TaskProgressRepository taskProgressRepository;
    private ConnectTaskStatusCommandHandler handler;

    @BeforeEach
    void setup() {
        var projectPartitionRepository = mock(ProjectPartitionRepository.class);
        var loggedUserAccessor = mock(LoggedUserAccessor.class);
        taskStatusRepository = mock(TaskStatusRepository.class);
        taskProgressRepository = mock(TaskProgressRepository.class);
        handler = new ConnectTaskStatusCommandHandler(taskStatusRepository, taskProgressRepository);
        ReflectionTestUtils.setField(handler, "projectPartitionRepository", projectPartitionRepository);
        ReflectionTestUtils.setField(handler, "loggedUserAccessor", loggedUserAccessor);

        when(loggedUserAccessor.tryGetPartitionId())
                .thenReturn(OptionalInt.of(PARTITION_ID));
        when(projectPartitionRepository.findById(PARTITION_ID))
                .thenReturn(Optional.of(new ProjectPartition() {{ setId(PARTITION_ID); }}));
    }

    @Test
    void handleInternal_updatesExistingProgress() {
        var from = mock(TaskStatus.class);
        var to = mock(TaskStatus.class);

        when(from.getId()).thenReturn(1L);
        when(to.getId()).thenReturn(2L);

        var command = ConnectTaskStatusCommand.builder()
                .kanbanBoardId(10)
                .connections(List.of(Connection.builder()
                        .fromStatusId(1)
                        .toStatusId(2)
                        .statusName("Updated")
                        .build()))
                .build();

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(10, Set.of(1L, 2L)))
                .thenReturn(List.of(from, to));

        var existing = new TaskProgress();
        when(taskProgressRepository.findByFromStatusAndNextStatus(from, to))
                .thenReturn(Optional.of(existing));

        assertNull(existing.getProgressionName());
        handler.handle(command);

        assertEquals("Updated", existing.getProgressionName());
        verify(taskProgressRepository).save(existing);
    }

    @Test
    void handleInternal_createsNewProgress() {
        var from = mock(TaskStatus.class);
        var to = mock(TaskStatus.class);
        when(from.getId()).thenReturn(1L);
        when(to.getId()).thenReturn(2L);

        var command = ConnectTaskStatusCommand.builder()
                .kanbanBoardId(10)
                .connections(List.of(Connection.builder()
                        .fromStatusId(1)
                        .toStatusId(2)
                        .statusName("NewName")
                        .build()))
                .build();

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(10, Set.of(1L, 2L)))
                .thenReturn(List.of(from, to));

        when(taskProgressRepository.findByFromStatusAndNextStatus(from, to))
                .thenReturn(Optional.empty());

        handler.handle(command);

        ArgumentCaptor<TaskProgress> captor = ArgumentCaptor.forClass(TaskProgress.class);
        verify(taskProgressRepository).save(captor.capture());
        var saved = captor.getValue();

        assertEquals(from, saved.getFromStatus());
        assertEquals(to, saved.getNextStatus());
        assertEquals("NewName", saved.getProgressionName());
    }

    @Test
    void handleInternal_missingStatuses_throws() {
        var command = ConnectTaskStatusCommand.builder()
                .kanbanBoardId(10)
                .connections(List.of(Connection.builder()
                        .fromStatusId(1)
                        .toStatusId(2)
                        .statusName("X")
                        .build()))
                .build();

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(10, List.of(1L, 2L)))
                .thenReturn(List.of()); // not enough

        assertThrows(RequestException.class, () ->
                handler.handle(command)
        );
    }
}
