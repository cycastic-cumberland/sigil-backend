package net.cycastic.sigil.controller.pm;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequireTenantId
@RequirePartitionId
@RequiredArgsConstructor
@RequestMapping("api/pm/tasks")
public class TasksController {
}
