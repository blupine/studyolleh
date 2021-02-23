package com.studyolleh.modules.notification.service;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.notification.repository.NotificationRepository;
import com.studyolleh.modules.notification.domain.Notification;
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
        notificationRepository.saveAll(notifications); // READ-ONLY로 Repository에서 읽어온거라서?
    }

    public void deleteCheckedNotification(Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
    }
}
