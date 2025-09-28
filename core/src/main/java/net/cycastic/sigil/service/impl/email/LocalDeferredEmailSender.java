package net.cycastic.sigil.service.impl.email;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.service.email.DeferredEmailSender;
import net.cycastic.sigil.service.email.EmailImage;
import net.cycastic.sigil.service.impl.ApplicationEmailSender;
import org.springframework.core.task.TaskExecutor;

import java.util.Map;

@RequiredArgsConstructor
public class LocalDeferredEmailSender implements DeferredEmailSender {
    private final TaskExecutor taskScheduler;
    private final ApplicationEmailSender applicationEmailSender;

    @Override
    public void sendHtml(String fromAddress, String fromName, String to, String cc, String subject, String htmlBody, @Nullable Map<String, ? extends EmailImage> imageStreamSource) {
        taskScheduler.execute(() -> applicationEmailSender.sendHtml(fromAddress, fromName, to, cc, subject, htmlBody, imageStreamSource));
    }

    @Override
    public void sendHtml(String to, String cc, String subject, String htmlBody, @Nullable Map<String, EmailImage> imageStreamSource) {
        taskScheduler.execute(() -> applicationEmailSender.sendHtml(to, cc, subject, htmlBody, imageStreamSource));
    }
}
