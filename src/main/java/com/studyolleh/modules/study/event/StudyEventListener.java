package com.studyolleh.modules.study.event;

import com.studyolleh.infra.config.AppProperties;
import com.studyolleh.infra.mail.EmailMessage;
import com.studyolleh.infra.mail.EmailService;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.modules.notification.domain.Notification;
import com.studyolleh.modules.notification.repository.NotificationRepository;
import com.studyolleh.modules.notification.domain.NotificationType;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.repository.StudyRepository;
import com.studyolleh.modules.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final StudyService studyService;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findByTagsAndZones(studyService.getStudyTags(study), studyService.getStudyZones(study));

        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()) {
                String emailSubject = "스터디올래, '" + study.getTitle() + "' 스터디가 생겼습니다.";
                String contextMessage = "새로운 스터디가 생겼습니다.";
                sendStudyEventEmail(study, account, contextMessage, emailSubject);
            }

            if (account.isStudyCreatedByWeb()) {
                saveStudyEventNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });
    }

    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithMembersAndMAnagersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(studyService.getStudyMembers(study));
        accounts.addAll(studyService.getStudyManagers(study));

        accounts.forEach(account -> {
            if (account.isStudyUpdatedByEmail()) {
                String emailSubject = "스터디올래, '" + study.getTitle() + "' 스터디에 새소식이 있습니다..";
                String contextMessage = studyUpdateEvent.getMessage();
                sendStudyEventEmail(study, account, contextMessage, emailSubject);
            }

            if (account.isStudyUpdatedByWeb()) {
                saveStudyEventNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }
        });
    }

    private void saveStudyEventNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = Notification.builder()
                .title(study.getTitle())
                .link("/study/" + study.getEncodedPath())
                .message(message)
                .checked(false)
                .account(account)
                .createdLocalDateTime(LocalDateTime.now())
                .notificationType(notificationType)
                .build();
        notificationRepository.save(notification);
    }

    private void sendStudyEventEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }
}
