package net.cycastic.sigil.domain.dto.pm;

import lombok.Builder;
import lombok.Data;
import net.cycastic.sigil.domain.model.pm.KanbanBoard;

@Data
@Builder
public class KanbanBoardDto {
    private String boardName;

    public static KanbanBoardDto fromDomain(KanbanBoard domain){
        return KanbanBoardDto.builder()
                .boardName(domain.getBoardName())
                .build();
    }
}
