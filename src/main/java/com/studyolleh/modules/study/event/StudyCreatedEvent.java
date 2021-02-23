package com.studyolleh.modules.study.event;

import com.studyolleh.modules.study.domain.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent {

    private final Study study;

}
