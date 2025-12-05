package net.cycastic.sigil.controller.admin;

import an.awesome.pipelinr.Pipelinr;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/admin/users")
public class AdminUsersController {
    private final Pipelinr pipelinr;
}
