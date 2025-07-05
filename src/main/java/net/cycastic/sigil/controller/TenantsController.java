package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.tenant.delete.DeleteTenantCommand;
import net.cycastic.sigil.application.tenant.get.GetTenantCipherCommand;
import net.cycastic.sigil.application.tenant.get.GetTenantCommand;
import net.cycastic.sigil.application.tenant.query.QueryTenantCommand;
import net.cycastic.sigil.application.tenant.save.SaveTenantCommand;
import net.cycastic.sigil.domain.dto.CipherDto;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.TenantDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/tenants")
public class TenantsController {
    private final Pipelinr pipelinr;

    @GetMapping("tenant")
    public TenantDto getTenant(GetTenantCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("tenant/cipher")
    public CipherDto getCipher(GetTenantCipherCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("tenant")
    public IdDto saveTenant(@RequestBody SaveTenantCommand command){
        return pipelinr.send(command);
    }

    @DeleteMapping("tenant")
    public void deleteTenant(DeleteTenantCommand command){
        pipelinr.send(command);
    }

    @GetMapping
    public PageResponseDto<TenantDto> queryTenants(QueryTenantCommand command){
        return pipelinr.send(command);
    }
}
