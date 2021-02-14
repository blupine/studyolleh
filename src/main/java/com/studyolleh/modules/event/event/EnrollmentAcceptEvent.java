package com.studyolleh.modules.event.event;


import com.studyolleh.modules.event.Enrollment;

public class EnrollmentAcceptEvent extends EnrollmentEvent{

    public EnrollmentAcceptEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 확인했습니다. 모임에 참석하세요.");
    }

}
