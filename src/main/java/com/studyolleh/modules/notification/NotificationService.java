package com.studyolleh.modules.notification;

import com.studyolleh.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getNotificationsByAccountAndChecked(Account account, boolean checked) {
        return notificationRepository.findByAccountAndCheckedOrderByCreatedLocalDateTime(account, checked);
    }

    public Long getNotificationCountByAccountAndChecked(Account account, boolean checked) {
        return notificationRepository.countByAccountAndChecked(account, checked);
    }

    public void markAsRead(List<Notification> notifications) {
        notifications.forEach(n -> n.setChecked(true));
        notificationRepository.saveAll(notifications);
    }

    public void deleteCheckedNotification(Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
    }
}
