package com.studyolleh.modules.event.event;

import com.studyolleh.modules.event.domain.Enrollment;

public class EnrollmentRejectEvent extends EnrollmentEvent{

    public EnrollmentRejectEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청이 거절됐습니다.");
    }

}
