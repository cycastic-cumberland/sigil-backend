package net.cycastic.sigil.application.admin.user.list;

import an.awesome.pipelinr.Command;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.cycastic.sigil.application.user.validation.admin.RequireAdmin;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.paging.PageRequestDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;

@Data
@RequireAdmin
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListUsersCommand extends PageRequestDto implements Command<PageResponseDto<UserDto>> {
    @Nullable
    private String contentTerm;
}
