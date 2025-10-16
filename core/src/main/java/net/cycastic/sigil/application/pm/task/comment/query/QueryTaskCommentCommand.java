package net.cycastic.sigil.application.pm.task.comment.query;

import an.awesome.pipelinr.Command;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.pm.CommentDto;
import org.springframework.data.domain.Sort;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryTaskCommentCommand extends PageRequestDto implements Command<PageResponseDto<CommentDto>> {
    @NotEmpty
    private String taskId;

    @Override
    protected Sort getDefaultSort() {
        return Sort.by("createdAt").descending();
    }

    @JsonIgnore
    public boolean isCacheable(){
        return getPage() == 1 && getPageSize() == 10 && (getOrderBy() == null || "createdAt:desc".equals(getOrderBy()));
    }
}
