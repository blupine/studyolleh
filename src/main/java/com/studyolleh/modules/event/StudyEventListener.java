package com.studyolleh.modules.event;

import com.studyolleh.modules.event.StudyCreatedEvent;
import com.studyolleh.modules.study.Study;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Async
@Component
@Transactional(readOnly = true)
public class StudyEventListener {

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyCreatedEvent.getStudy();
        log.info(study.getTitle() + " is created.");

        // TODO : Email을 보내거나 DB에 Notification 정보를 저장

        throw new RuntimeException();
    }

}