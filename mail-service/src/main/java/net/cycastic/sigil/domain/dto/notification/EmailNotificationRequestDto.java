package net.cycastic.sigil.domain.dto.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmailNotificationRequestDto extends NotificationRequestDto{
    private String fromAddress;
    private String fromName;
    private String to;
    private String cc;
    private String subject;
    private String htmlBody;
    private Map<String, EmailImageDto> imageStreamSource;
}
