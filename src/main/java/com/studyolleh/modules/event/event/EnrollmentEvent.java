package com.studyolleh.modules.event.event;

import com.studyolleh.modules.event.domain.Enrollment;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public abstract class EnrollmentEvent {

    protected final Enrollment enrollment;

    protected final String message;

}
