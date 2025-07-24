package net.cycastic.sigil.application.auth.invalidatesessions;

import an.awesome.pipelinr.Command;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidateAllSessionsCommand implements Command<Void> {
    private int userId;
}
