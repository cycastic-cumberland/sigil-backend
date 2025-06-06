package net.cycastic.portfoliotoolkit.controller;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import net.cycastic.portfoliotoolkit.application.sample.helloworld.HelloWorldCommand;
import net.cycastic.portfoliotoolkit.controller.annotation.RequireProjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireProjectId
@RequiredArgsConstructor
@RequestMapping("api/hello")
public class HelloController {
    private final Pipelinr pipelinr;

    @GetMapping("/example")
    public String example() {
        return pipelinr.send(new HelloWorldCommand());
    }
}
