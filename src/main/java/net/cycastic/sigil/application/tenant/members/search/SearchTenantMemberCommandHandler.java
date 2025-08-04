package net.cycastic.sigil.application.tenant.members.search;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.TenantService;
import net.cycastic.sigil.domain.ApplicationConstants;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import net.cycastic.sigil.domain.dto.tenant.TenantUserDto;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchTenantMemberCommandHandler implements Command.Handler<SearchTenantMemberCommand, PageResponseDto<TenantUserDto>> {
    private final TenantUserRepository tenantUserRepository;
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantService tenantService;

    @Override
    public PageResponseDto<TenantUserDto> handle(SearchTenantMemberCommand command) {
        tenantService.checkPermission(ApplicationConstants.TenantPermissions.LIST_USERS);
        var contentTerm = command.getContentTerm();

        Page<TenantUserRepository.TenantUserItem> page;
        if (contentTerm != null){
            contentTerm = contentTerm
                    .replace("\\", "\\\\")
                    .replace("_", "\\_")
                    .replace("%", "\\%");
            page = tenantUserRepository.findItemsByContentTerm(loggedUserAccessor.getTenantId(),
                    contentTerm,
                    loggedUserAccessor.getUserId(),
                    command.toPageable());
        } else {
            page = tenantUserRepository.findAllItems(loggedUserAccessor.getTenantId(), command.toPageable());
        }
        return PageResponseDto.fromDomain(page, TenantUserDto::fromDomain);
    }
}
