package net.cycastic.sigil.application.tenant.members.search;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.repository.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTenantMemberEmailByPrefixCommandHandler implements Command.Handler<SearchTenantMemberEmailByPrefixCommand, PageResponseDto<String>> {
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantService tenantService;

    @Override
    public PageResponseDto<String> handle(SearchTenantMemberEmailByPrefixCommand command) {
        tenantService.checkPermission(ApplicationConstants.TenantPermissions.LIST_USERS);
        var prefix = command.getEmailPrefix();
        prefix = prefix
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");

        var page = tenantUserRepository.findByEmailPrefix(loggedUserAccessor.getTenantId(),
                prefix,
                loggedUserAccessor.getUserId(),
                command.toPageable());
        return PageResponseDto.fromDomain(page, TenantUserRepository.TenantUserEmailItem::getEmail);
    }
}
