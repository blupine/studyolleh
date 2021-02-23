package com.studyolleh.modules.notification.controller;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.authentication.CurrentAccount;
import com.studyolleh.modules.notification.service.NotificationService;
import com.studyolleh.modules.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationService.getNotificationsByAccountAndChecked(account, false);
        Long numberOfChecked = notificationService.getNotificationCountByAccountAndChecked(account, true);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("numberOfNotChecked", notifications.size());
        putCategorizedNotifications(model, notifications);
        model.addAttribute("isNew", true);
        notificationService.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotification(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationService.getNotificationsByAccountAndChecked(account, true);
        Long numberOfNotChecked = notificationService.getNotificationCountByAccountAndChecked(account, false);
        model.addAttribute("numberOfChecked", notifications.size());
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        putCategorizedNotifications(model, notifications);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentAccount Account account) {
        notificationService.deleteCheckedNotification(account);
        return "redirect:/notifications";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications) {
        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingStudyNotifications = new ArrayList<>();

        for (Notification notification : notifications) {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED:
                    newStudyNotifications.add(notification);
                    break;
                case EVENT_ENROLLMENT:
                    eventEnrollmentNotifications.add(notification);
                    break;
                case STUDY_UPDATED:
                    watchingStudyNotifications.add(notification);
                    break;
            }
        }

        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    }
}
