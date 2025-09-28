package net.cycastic.sigil.service.impl;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.notification.EmailNotificationRequestDto;
import net.cycastic.sigil.domain.dto.notification.NotificationRequestDto;
import net.cycastic.sigil.service.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SQSNotifierService {
    private static final Logger logger = LoggerFactory.getLogger(SQSNotifierService.class);
    private final EmailSender emailSender;
    private final ObjectMapper objectMapper;

    private void processEmailNotification(EmailNotificationRequestDto request){
        emailSender.sendHtml(request.getFromAddress(),
                request.getFromName(),
                request.getTo(),
                request.getCc(),
                request.getSubject(),
                request.getHtmlBody(),
                request.getImageStreamSource());
    }

    public SQSBatchResponse process(SQSEvent event){
        var errors = new ArrayList<SQSBatchResponse.BatchItemFailure>();
        for (var record : event.getRecords()){
            try {
                var request = objectMapper.readValue(record.getBody(), NotificationRequestDto.class);
                switch (request.getType()){
                    case EMAIL -> processEmailNotification(objectMapper.readValue(record.getBody(), EmailNotificationRequestDto.class));
                    case APP -> throw new RuntimeException("Unimplemented");
                    default -> throw new IllegalStateException("Illegal request type");
                }
            } catch (Exception e){
                logger.error("Failed to process event for message {}", record.getMessageId(), e);
                errors.add(new SQSBatchResponse.BatchItemFailure(record.getMessageId()));
            }
        }
        return errors.isEmpty() ? new SQSBatchResponse() : new SQSBatchResponse(errors);
    }
}
