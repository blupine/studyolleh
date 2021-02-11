package com.studyolleh.modules.study.event;

import com.studyolleh.infra.config.AppProperties;
import com.studyolleh.infra.mail.EmailMessage;
import com.studyolleh.infra.mail.EmailService;
import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.account.AccountPredicates;
import com.studyolleh.modules.account.AccountRepository;
import com.studyolleh.modules.notification.Notification;
import com.studyolleh.modules.notification.NotificationRepository;
import com.studyolleh.modules.notification.NotificationType;
import com.studyolleh.modules.study.Study;
import com.studyolleh.modules.study.StudyRepository;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZoens(study.getTags(), study.getZones()));

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                sendStudyCreatedEmail(study, account);
            }

            if (account.isStudyCreatedByWeb()) {
                saveStudyCreatedNotification(study, account);
            }
        });
    }

    private void saveStudyCreatedNotification(Study study, Account account) {
        Notification notification = Notification.builder()
                .title(study.getTitle())
                .link("/study/" + study.getEncodedPath())
                .message(study.getShortDescription())
                .checked(false)
                .account(account)
                .createdLocalDateTime(LocalDateTime.now())
                .notificationType(NotificationType.STUDY_CREATED)
                .build();
        notificationRepository.save(notification);
    }

    private void sendStudyCreatedEmail(Study study, Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", "새로운 스터디가 생겼습니다.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("스터디올래, '" + study.getTitle() + "' 스터디가 생겼습니다.")
                .to(account.getEmail())
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }
}
