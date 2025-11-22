package net.cycastic.sigil.application.pm.task;

import net.cycastic.sigil.application.pm.task.status.TaskStatusService;
import net.cycastic.sigil.application.pm.task.transit.MoveTasksCommand;
import net.cycastic.sigil.application.pm.task.transit.MoveTasksCommandHandler;
import net.cycastic.sigil.domain.dto.pm.TaskProgressDto;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.Task;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.TaskRepository;
import net.cycastic.sigil.domain.repository.pm.TaskStatusRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MoveTasksCommandHandlerTest {
    private TaskRepository taskRepository;
    private LoggedUserAccessor loggedUserAccessor;
    private TaskStatusRepository taskStatusRepository;
    private TaskStatusService taskStatusService;
    private MoveTasksCommandHandler handler;

    @BeforeEach
    void setup() {
        taskRepository = mock(TaskRepository.class);
        loggedUserAccessor = mock(LoggedUserAccessor.class);
        taskStatusRepository = mock(TaskStatusRepository.class);
        taskStatusService = mock(TaskStatusService.class);

        handler = new MoveTasksCommandHandler(
                taskRepository,
                loggedUserAccessor,
                taskStatusRepository,
                taskStatusService
        );
    }

    @Test
    void handle_movesNextTaskSuccessfully() {
        final var tenantId = 10;
        final var boardId = 99;

        when(loggedUserAccessor.getTenantId()).thenReturn(tenantId);

        var cmd = MoveTasksCommand.builder()
                .kanbanBoardId(boardId)
                .tasks(Map.of("T-1", 200L))
                .build();

        // domain objects
        var currentStatus = new TaskStatus();
        currentStatus.setId(100L);

        var targetStatus = new TaskStatus();
        targetStatus.setId(200L);

        var targetMap = Map.of(200L, targetStatus);

        var task = new Task();
        task.setTaskIdentifier("T-1");
        task.setTaskStatus(currentStatus);

        when(taskRepository.findByTenant_IdAndTaskIdentifierIn(
                tenantId, cmd.getTasks().keySet()))
                .thenReturn(List.of(task));

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(Mockito.anyInt(), Mockito.anyCollection()))
                .then(a -> {
                    assertEquals(boardId, (Integer) a.getArgument(0));
                    Collection<Long> ids = a.getArgument(1);
                    return ids.stream()
                            .map(targetMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                });

        // transitions: 100 -> 200 is allowed
        var mappingPair = new TaskStatusService.TaskStatusMappingPair(
                Map.of(100L, List.of(TaskProgressDto.builder()
                                .fromStatusId(100)
                                .toStatusId(200)
                                .name("T-T")
                        .build())),
                Map.of()
        );
        when(taskStatusService.toMappingPair(any())).thenReturn(mappingPair);

        // act
        handler.handle(cmd);

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(saved.capture());

        assertEquals(200L, saved.getValue().getTaskStatus().getId());
    }

    @Test
    void handle_movesPrevTaskSuccessfully() {
        final var tenantId = 10;
        final var boardId = 99;

        when(loggedUserAccessor.getTenantId()).thenReturn(tenantId);

        var cmd = MoveTasksCommand.builder()
                .kanbanBoardId(boardId)
                .tasks(Map.of("T-1", 100L))
                .build();

        // domain objects
        var currentStatus = new TaskStatus();
        currentStatus.setId(200L);

        var targetStatus = new TaskStatus();
        targetStatus.setId(100L);

        var targetMap = Map.of(100L, targetStatus);

        var task = new Task();
        task.setTaskIdentifier("T-1");
        task.setTaskStatus(currentStatus);

        when(taskRepository.findByTenant_IdAndTaskIdentifierIn(
                tenantId, cmd.getTasks().keySet()))
                .thenReturn(List.of(task));

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(Mockito.anyInt(), Mockito.anyCollection()))
                .then(a -> {
                    assertEquals(boardId, (Integer) a.getArgument(0));
                    Collection<Long> ids = a.getArgument(1);
                    return ids.stream()
                            .map(targetMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                });

        // transitions: 100 -> 200 is allowed
        var mappingPair = new TaskStatusService.TaskStatusMappingPair(
                Map.of(),
                Map.of(200L, List.of(TaskProgressDto.builder()
                        .fromStatusId(100)
                        .toStatusId(200)
                        .name("T-T")
                        .build()))
        );
        when(taskStatusService.toMappingPair(any())).thenReturn(mappingPair);

        // act
        handler.handle(cmd);

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(saved.capture());

        assertEquals(100L, saved.getValue().getTaskStatus().getId());
    }

    @Test
    void handle_throws_whenStatusNotAllowed() {
        final var tenantId = 10;
        final var boardId = 99;

        when(loggedUserAccessor.getTenantId()).thenReturn(tenantId);

        var cmd = MoveTasksCommand.builder()
                .kanbanBoardId(boardId)
                .tasks(Map.of("T-1", 200L))
                .build();

        var currentStatus = new TaskStatus();
        currentStatus.setId(100L);

        var targetStatus = new TaskStatus();
        targetStatus.setId(200L);

        var targetMap = Map.of(200L, targetStatus);

        var task = new Task();
        task.setTaskIdentifier("T-1");
        task.setTaskStatus(currentStatus);

        when(taskRepository.findByTenant_IdAndTaskIdentifierIn(
                tenantId, cmd.getTasks().keySet()))
                .thenReturn(List.of(task));

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(Mockito.anyInt(), Mockito.anyCollection()))
                .then(a -> {
                    assertEquals(boardId, (Integer) a.getArgument(0));
                    Collection<Long> ids = a.getArgument(1);
                    return ids.stream()
                            .map(targetMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                });

        // no allowed transitions at all
        var mappingPair = new TaskStatusService.TaskStatusMappingPair(Map.of(), Map.of());
        when(taskStatusService.toMappingPair(any())).thenReturn(mappingPair);

        assertThrows(RequestException.class, () -> handler.handle(cmd));
    }
}
