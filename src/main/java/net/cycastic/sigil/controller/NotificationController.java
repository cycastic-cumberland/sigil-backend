package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.notifications.filter.get.GetNotificationSettingsCommand;
import net.cycastic.sigil.application.notifications.filter.save.SaveNotificationSettingsCommand;
import net.cycastic.sigil.application.notifications.get.GetNotificationsCommand;
import net.cycastic.sigil.application.notifications.get.GetUnreadNotificationCountCommand;
import net.cycastic.sigil.application.notifications.mark.MarkNotificationsAsReadCommand;
import net.cycastic.sigil.application.notifications.remove.DeleteNotificationsCommand;
import net.cycastic.sigil.domain.dto.CountDto;
import net.cycastic.sigil.domain.dto.notifications.NotificationSettingsDto;
import net.cycastic.sigil.domain.dto.notifications.NotificationsDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {
    private final Pipelinr pipelinr;

    @GetMapping
    public NotificationsDto getNotifications(@Valid GetNotificationsCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("unread-count")
    public CountDto countUnreadNotifications(@Valid GetUnreadNotificationCountCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("settings")
    public NotificationSettingsDto getNotificationSettings(@Valid GetNotificationSettingsCommand command){
        return pipelinr.send(command);
    }

    @PostMapping("settings")
    public void saveNotificationSettings(@Valid SaveNotificationSettingsCommand command){
        pipelinr.send(command);
    }

    @PostMapping
    public void markAsRead(@Valid MarkNotificationsAsReadCommand command){
        pipelinr.send(command);
    }

    @DeleteMapping
    public void deleteNotification(@Valid DeleteNotificationsCommand command){
        pipelinr.send(command);
    }
}
