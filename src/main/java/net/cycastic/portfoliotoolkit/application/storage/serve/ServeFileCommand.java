package net.cycastic.portfoliotoolkit.application.storage.serve;

import an.awesome.pipelinr.Command;
import lombok.Data;

import java.util.UUID;

@Data
public class ServeFileCommand implements Command<ServeFileCommandResponse> {
    private int projectId;
    private int userId;
    private UUID shareToken;
    private String path;
}
