package net.cycastic.sigil.application.pm.task;

import net.cycastic.sigil.application.pm.task.status.transit.connect.SaveTaskStatusProgressionCommand;
import net.cycastic.sigil.application.pm.task.status.transit.connect.SaveTaskStatusProgressionCommandHandler;
import net.cycastic.sigil.application.pm.task.status.transit.connect.Connection;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.model.pm.KanbanBoard;
import net.cycastic.sigil.domain.model.pm.ProjectPartition;
import net.cycastic.sigil.domain.model.pm.TaskProgress;
import net.cycastic.sigil.domain.model.pm.TaskStatus;
import net.cycastic.sigil.domain.repository.pm.KanbanBoardRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SaveTaskStatusProgressionCommandHandlerTest {
    private static final int BOARD_ID = 10;
    private static final int PARTITION_ID = 67;

    private TaskStatusRepository taskStatusRepository;
    private TaskProgressRepository taskProgressRepository;
    private SaveTaskStatusProgressionCommandHandler handler;
    private KanbanBoard board;

    @BeforeEach
    void setup() {
        var projectPartitionRepository = mock(ProjectPartitionRepository.class);
        var loggedUserAccessor = mock(LoggedUserAccessor.class);
        var kanbanBoardRepository = mock(KanbanBoardRepository.class);
        taskStatusRepository = mock(TaskStatusRepository.class);
        taskProgressRepository = mock(TaskProgressRepository.class);
        handler = new SaveTaskStatusProgressionCommandHandler(taskStatusRepository, taskProgressRepository, kanbanBoardRepository);
        ReflectionTestUtils.setField(handler, "projectPartitionRepository", projectPartitionRepository);
        ReflectionTestUtils.setField(handler, "loggedUserAccessor", loggedUserAccessor);

        when(loggedUserAccessor.tryGetPartitionId())
                .thenReturn(OptionalInt.of(PARTITION_ID));
        when(projectPartitionRepository.findById(PARTITION_ID))
                .thenReturn(Optional.of(new ProjectPartition() {{ setId(PARTITION_ID); }}));

        board = KanbanBoard.builder()
                .id(BOARD_ID)
                .build();

        when(kanbanBoardRepository.findByIdAndProjectPartition_Id(eq(BOARD_ID), eq(PARTITION_ID)))
                .thenReturn(Optional.of(board));
    }

    private SaveTaskStatusProgressionCommand createCmd(int boardId, List<Connection> connections) {
        SaveTaskStatusProgressionCommand cmd = mock(SaveTaskStatusProgressionCommand.class);
        when(cmd.getKanbanBoardId()).thenReturn(boardId);
        when(cmd.getConnections()).thenReturn(connections);
        return cmd;
    }

    private TaskStatus status(long id) {
        return TaskStatus.builder()
                .id(id)
                .build();
    }

    private TaskProgress progress(TaskStatus from, TaskStatus to, String name) {
        return TaskProgress.builder()
                .fromStatus(from)
                .nextStatus(to)
                .progressionName(name)
                .build();
    }

    // ---------------------------------------------------------------
    // 1. Board not found
    // ---------------------------------------------------------------
    @Test
    void boardNotFound_throws404() {
        var cmd = createCmd(1, List.of());

        var ex = assertThrows(RequestException.class, () ->
                handler.handle(cmd)
        );

        assertEquals(404, ex.getResponseCode());
        assertEquals("Board not found", ex.getMessage());
    }

    // ---------------------------------------------------------------
    // 2. Some statuses missing
    // ---------------------------------------------------------------
    @Test
    void missingStatuses_throws404() {
        var cmd = createCmd(BOARD_ID, List.of(
                new Connection(11L, 22L, "X")
        ));

        // missing one
        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(eq(BOARD_ID), anySet()))
                .thenReturn(List.of(status(11L)));

        var ex = assertThrows(RequestException.class, () ->
                handler.handle(cmd)
        );

        assertEquals(404, ex.getResponseCode());
        assertEquals("Some statuses could not be found", ex.getMessage());
    }

    // ---------------------------------------------------------------
    // 3. No diff at all
    // ---------------------------------------------------------------
    @Test
    void noChanges_noSavesOrDeletes() {
        var c = new Connection(1L, 2L, "Done");

        var cmd = createCmd(BOARD_ID, List.of(c));

        var s1 = status(1L);
        var s2 = status(2L);

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(eq(BOARD_ID), anySet()))
                .thenReturn(List.of(s1, s2));

        var existing = progress(s1, s2, "Done");

        when(taskProgressRepository.findByFromStatus_KanbanBoard(board))
                .thenReturn(List.of(existing));

        handler.handle(cmd);

        verify(taskProgressRepository, times(2)).saveAll(List.of()); // no new
        verify(taskProgressRepository).deleteAll(List.of()); // no deletes
    }


    // ---------------------------------------------------------------
    // 4. Only new progresses added
    // ---------------------------------------------------------------
    @Test
    void newProgressesAdded() {
        var c = new Connection(1L, 2L, "A→B");
        var cmd = createCmd(BOARD_ID, List.of(c));

        var s1 = status(1L);
        var s2 = status(2L);

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(eq(BOARD_ID), anySet()))
                .thenReturn(List.of(s1, s2));

        when(taskProgressRepository.findByFromStatus_KanbanBoard(board))
                .thenReturn(List.of()); // nothing existing

        handler.handle(cmd);

        ArgumentCaptor<List<TaskProgress>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskProgressRepository, times(2)).saveAll(captor.capture());
        assertEquals(1, captor.getAllValues().getFirst().size());
        verify(taskProgressRepository).deleteAll(List.of());
    }

    // ---------------------------------------------------------------
    // 5. Update progression name
    // ---------------------------------------------------------------
    @Test
    void updateProgressName() {
        var c = new Connection(1L, 2L, "Updated");

        var cmd = createCmd(BOARD_ID, List.of(c));

        var s1 = status(1L);
        var s2 = status(2L);

        var existing = progress(s1, s2, "Old");

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(eq(BOARD_ID), anySet()))
                .thenReturn(List.of(s1, s2));

        when(taskProgressRepository.findByFromStatus_KanbanBoard(board))
                .thenReturn(List.of(existing));

        handler.handle(cmd);

        verify(taskProgressRepository).deleteAll(List.of()); // none deleted

        ArgumentCaptor<List<TaskProgress>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskProgressRepository, times(2)).saveAll(captor.capture());
        assertEquals(0, captor.getAllValues().getFirst().size());
        assertEquals(List.of(existing), captor.getAllValues().get(1));
        assertEquals("Updated", captor.getAllValues().get(1).getFirst().getProgressionName());
    }

    // ---------------------------------------------------------------
    // 6. Deleted progresses
    // ---------------------------------------------------------------
    @Test
    void deleteProgress() {
        var c = new Connection(1L, 2L, "X");

        var cmd = createCmd(BOARD_ID, List.of(c));

        var s1 = status(1L);
        var s2 = status(2L);
        var s3 = status(3L);
        var s4 = status(4L);

        var kept = progress(s1, s2, "X");
        var deleted = progress(s3, s4, "Y");

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(eq(BOARD_ID), anySet()))
                .thenReturn(List.of(s1, s2));

        when(taskProgressRepository.findByFromStatus_KanbanBoard(board))
                .thenReturn(List.of(kept, deleted));

        handler.handle(cmd);

        verify(taskProgressRepository).deleteAll(List.of(deleted));
    }

    // ---------------------------------------------------------------
    // 7. Mixed: add + update + delete
    // ---------------------------------------------------------------
    @Test
    void mixedScenario() {
        var cmd = createCmd(BOARD_ID, List.of(
                new Connection(1L, 2L, "NameA"), // update name
                new Connection(1L, 3L, "NewOne") // new
        ));

        var s1 = status(1L);
        var s2 = status(2L);
        var s3 = status(3L);

        var existingSamePairButOldName = progress(s1, s2, "OldName");
        var toDelete = progress(s2, s3, "SomethingElse");

        when(taskStatusRepository.findByKanbanBoard_IdAndIdIn(eq(BOARD_ID), anySet()))
                .thenReturn(List.of(s1, s2, s3));

        when(taskProgressRepository.findByFromStatus_KanbanBoard(board))
                .thenReturn(List.of(existingSamePairButOldName, toDelete));

        handler.handle(cmd);

        verify(taskProgressRepository).deleteAll(List.of(toDelete));
        ArgumentCaptor<List<TaskProgress>> captor = ArgumentCaptor.forClass(List.class);
        verify(taskProgressRepository, times(2)).saveAll(captor.capture());
        {
            var newList = captor.getAllValues().getFirst();
            assertEquals(1, newList.size());
            assertEquals("NewOne", newList.getFirst().getProgressionName());
        }
        {
            var newList = captor.getAllValues().get(1);
            assertEquals(1, newList.size());
            assertEquals("NameA", newList.getFirst().getProgressionName());
        }
    }
}
