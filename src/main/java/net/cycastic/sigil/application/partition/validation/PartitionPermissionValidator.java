package net.cycastic.sigil.application.partition.validation;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.PartitionService;
import net.cycastic.sigil.application.validation.CommandValidator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class PartitionPermissionValidator implements CommandValidator {
    private final PartitionService partitionService;

    @Override
    public void validate(Command command) {
        var annotation = Objects.requireNonNull(AnnotatedElementUtils.findMergedAnnotation(command.getClass(), PartitionPermission.class));
        partitionService.checkPermission(annotation.value());
    }

    @Override
    public boolean matches(Class klass) {
        return AnnotatedElementUtils.findMergedAnnotation(klass, PartitionPermission.class) != null;
    }
}
