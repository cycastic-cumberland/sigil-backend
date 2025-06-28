package net.cycastic.portfoliotoolkit.application.email.preview;

import an.awesome.pipelinr.Command;
import lombok.Data;
import net.cycastic.portfoliotoolkit.domain.model.EmailParameter;
import org.springframework.core.io.InputStreamSource;

@Data
public class PreviewEmailCommand implements Command<InputStreamSource> {
    private EmailParameter[] constants;
    private String templatePath;
}
