package net.cycastic.sigil.controller.admin;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.admin.entitlement.delete.DeleteEntitlementCommand;
import net.cycastic.sigil.application.admin.entitlement.get.GetEntitlementCommand;
import net.cycastic.sigil.application.admin.entitlement.list.ListEntitlementsCommand;
import net.cycastic.sigil.application.admin.entitlement.save.SaveEntitlementCommand;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.dto.EntitlementDto;
import net.cycastic.sigil.domain.dto.paging.EnumerablePage;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/admin/entitlements")
public class AdminEntitlementsController {
    private final Pipelinr pipelinr;

    @GetMapping("entitlement")
    public EntitlementDto getEntitlement(@Valid GetEntitlementCommand command){
        return pipelinr.send(command);
    }

    @GetMapping
    @RequireTenantId
    public EnumerablePage<EntitlementDto> listEntitlements(@Valid ListEntitlementsCommand command){
        return pipelinr.send(command);
    }

    @PostMapping
    public void saveEntitlement(@Valid @RequestBody SaveEntitlementCommand command){
        pipelinr.send(command);
    }

    @DeleteMapping
    public void deleteEntitlement(@Valid DeleteEntitlementCommand command){
        pipelinr.send(command);
    }
}
