package net.cycastic.sigil.application.user.sessions.invalidate;

import an.awesome.pipelinr.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvalidateAllSessionsCommand implements Command<Void> {
    private int userId;
}
