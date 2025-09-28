package net.cycastic.sigil.domain.dto.notification;

import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class EmailNotificationRequestDto extends NotificationRequestDto{
    private String fromAddress;
    private String fromName;
    private String to;
    private String cc;
    private String subject;

    private String region;
    private String bucketName;
    private String key;

    @PostConstruct
    private void postConstruct(){
        setType(NotificationRequestType.EMAIL);
    }
}
