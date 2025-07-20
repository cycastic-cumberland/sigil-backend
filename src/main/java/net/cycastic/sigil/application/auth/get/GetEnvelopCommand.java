package net.cycastic.sigil.application.auth.get;

import an.awesome.pipelinr.Command;
import net.cycastic.sigil.domain.dto.auth.EnvelopDto;

public class GetEnvelopCommand implements Command<EnvelopDto> {
    public static final GetEnvelopCommand INSTANCE = new GetEnvelopCommand();
}
