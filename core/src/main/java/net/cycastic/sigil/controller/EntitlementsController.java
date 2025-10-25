package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.feature.entitlement.delete.DeleteEntitlementCommand;
import net.cycastic.sigil.application.feature.entitlement.get.GetEntitlementCommand;
import net.cycastic.sigil.application.feature.entitlement.save.SaveEntitlementCommand;
import net.cycastic.sigil.domain.dto.EntitlementDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/entitlements")
public class EntitlementsController {
    private final Pipelinr pipelinr;

    @GetMapping("entitlement")
    public EntitlementDto getEntitlement(@Valid GetEntitlementCommand command){
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
