package net.cycastic.sigil.application.admin.user.list;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.repository.tenant.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListUsersCommandHandler implements Command.Handler<ListUsersCommand, PageResponseDto<UserDto>> {
    private final UserRepository userRepository;

    @Override
    public PageResponseDto<UserDto> handle(ListUsersCommand command) {
        String contentTerm = null;
        if (command.getContentTerm() != null){
            contentTerm = command.getContentTerm();
            contentTerm = contentTerm.replace("\\", "\\\\")
                    .replace("_", "\\_")
                    .replace("%", "\\%");
        }

        var page = userRepository.findUsersByContentTerm(contentTerm, command.toPageable());
        return PageResponseDto.fromDomain(page, UserDto::fromDomain);
    }
}
