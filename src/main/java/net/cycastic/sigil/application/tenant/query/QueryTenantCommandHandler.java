package net.cycastic.sigil.application.tenant.query;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.exception.ForbiddenException;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.TenantRepository;
import net.cycastic.sigil.domain.repository.UserRepository;
import net.cycastic.sigil.domain.dto.TenantDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryTenantCommandHandler implements Command.Handler<QueryTenantCommand, PageResponseDto<TenantDto>> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Override
    public PageResponseDto<TenantDto> handle(QueryTenantCommand command) {
        var userId = command.getUserId();
        if (userId != null &&
                !loggedUserAccessor.isAdmin() &&
                !userId.equals(loggedUserAccessor.getUserId())){
            throw new ForbiddenException();
        }

        if (userId == null){
            userId = loggedUserAccessor.getUserId();
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RequestException(404, "Could not find user"));
        var projects = tenantRepository.findByUser(user, command.toPageable());
        return PageResponseDto.fromDomain(projects, TenantDto::fromDomain);
    }
}
