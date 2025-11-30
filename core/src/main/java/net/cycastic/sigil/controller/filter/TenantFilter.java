package net.cycastic.sigil.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.controller.ApiExceptionHandler;
import net.cycastic.sigil.domain.exception.ExceptionResponse;
import net.cycastic.sigil.domain.exception.RequestException;
import net.cycastic.sigil.domain.repository.listing.PartitionUserRepository;
import net.cycastic.sigil.domain.repository.tenant.TenantUserRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import net.cycastic.sigil.service.serializer.JsonSerializer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {
    private final LoggedUserAccessor loggedUserAccessor;
    private final TenantUserRepository tenantUserRepository;
    private final PartitionUserRepository partitionUserRepository;
    private final ApiExceptionHandler apiExceptionHandler;
    private final JsonSerializer jsonSerializer;

    private void filter(){
        if (loggedUserAccessor.tryGetUserId().isEmpty()){
            return;
        }
        var tenantIdOpt = loggedUserAccessor.tryGetTenantId();
        var partitionIdOpt = loggedUserAccessor.tryGetPartitionId();
        if (tenantIdOpt.isEmpty()){
            if (partitionIdOpt.isPresent()){
                throw RequestException.forbidden();
            }
            return;
        }

        var tenantUser = tenantUserRepository.findByTenant_IdAndUser_Id(loggedUserAccessor.getTenantId(), loggedUserAccessor.getUserId())
                .orElseThrow(RequestException::forbidden);
        if (tenantUser.getLastInvited() != null){
            throw RequestException.forbidden();
        }

        if (partitionIdOpt.isEmpty()){
            return;
        }
        if (!partitionUserRepository.existsByPartition_Tenant_IdAndPartition_IdAndUser_Id(tenantIdOpt.getAsInt(),
                partitionIdOpt.getAsInt(),
                loggedUserAccessor.getUserId())){
            throw RequestException.forbidden();
        }
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filter();
        } catch (RequestException e){
            var exceptionResponse = apiExceptionHandler.toExceptionResponse(e, request);
            response.setStatus(exceptionResponse.getStatus());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            jsonSerializer.serialize(exceptionResponse, response.getOutputStream(), ExceptionResponse.class);
            response.flushBuffer();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
