package net.cycastic.sigil.domain.dto.notification;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequestDto{
    private String fromAddress;
    private String fromName;
    private String to;
    private String cc;
    private String subject;

    private String region;
    private String bucketName;
    private String key;

    public NotificationRequestType getType(){
        return NotificationRequestType.EMAIL;
    }
}
