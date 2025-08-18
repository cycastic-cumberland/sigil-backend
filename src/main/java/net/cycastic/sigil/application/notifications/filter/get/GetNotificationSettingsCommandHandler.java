package net.cycastic.sigil.application.notifications.filter.get;

import an.awesome.pipelinr.Command;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.domain.dto.notifications.NotificationSettingDto;
import net.cycastic.sigil.domain.dto.notifications.NotificationSettingsDto;
import net.cycastic.sigil.domain.repository.notifications.NotificationSettingRepository;
import net.cycastic.sigil.service.LoggedUserAccessor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetNotificationSettingsCommandHandler implements Command.Handler<GetNotificationSettingsCommand, NotificationSettingsDto> {
    private final LoggedUserAccessor loggedUserAccessor;
    private final NotificationSettingRepository notificationSettingRepository;

    @Override
    public NotificationSettingsDto handle(GetNotificationSettingsCommand command) {
        var currentFilters = notificationSettingRepository.findByUser_Id(loggedUserAccessor.getUserId(), Pageable.unpaged(Sort.by("id").ascending()));
        return NotificationSettingsDto.builder()
                .notificationSettings(currentFilters.stream()
                        .map(NotificationSettingDto::fromDomain)
                        .toList())
                .build();
    }
}
