package com.studyolleh.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("local") // spring profiles에서 active가 local일 때만 사용
@Slf4j
@Component
public class ConsoleMailService implements EmailService{
    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("sent email console : {}", emailMessage.getMessage());
    }
}
