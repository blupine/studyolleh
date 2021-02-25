package com.studyolleh.modules.event.event;

import com.studyolleh.infra.config.AppProperties;
import com.studyolleh.infra.mail.EmailMessage;
import com.studyolleh.infra.mail.EmailService;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.event.domain.Enrollment;
import com.studyolleh.modules.event.domain.Event;
import com.studyolleh.modules.notification.domain.Notification;
import com.studyolleh.modules.notification.repository.NotificationRepository;
import com.studyolleh.modules.notification.domain.NotificationType;
import com.studyolleh.modules.study.domain.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Study study = event.getStudy();

        if (account.isStudyEnrollmentResultByEmail()) {
            sendEmail(account, enrollmentEvent, event, study);
        }

        if (account.isStudyEnrollmentResultByWeb()) {
            saveStudyEnrollmentNotification(account, enrollmentEvent, event, study);
        }
    }

    private void saveStudyEnrollmentNotification(Account account, EnrollmentEvent enrollmentEvent, Event event, Study study) {
        Notification notification = Notification.builder()
                .title(study.getTitle() + " / " + event.getTitle())
                .link("/study/" + study.getEncodedPath() + "/events/" + event.getId())
                .message(enrollmentEvent.message)
                .checked(false)
                .account(account)
                .createdLocalDateTime(LocalDateTime.now())
                .notificationType(NotificationType.EVENT_ENROLLMENT)
                .build();
        notificationRepository.save(notification);
    }

    private void sendEmail(Account account, EnrollmentEvent enrollmentEvent, Event event, Study study) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("main/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("스터디올래, " + event.getTitle() + " 모임 참가 신청 결과")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}
