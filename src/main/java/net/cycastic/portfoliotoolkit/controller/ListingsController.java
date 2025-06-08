package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.listing.acp.create.SaveLACPCommand;
import net.cycastic.portfoliotoolkit.controller.annotation.RequireProjectId;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireProjectId
@RequiredArgsConstructor
@RequestMapping("api/listings")
public class ListingsController {
    private final Pipelinr pipelinr;

    @PostMapping("acp")
    public void saveLACP(@RequestBody SaveLACPCommand command){
        // TODO: refine approach
        throw new UnsupportedOperationException();
//        pipelinr.send(command);
    }
}
