package com.studyolleh.settings.form;

import com.studyolleh.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Notifications {
    private boolean studyCreatedByEmail; // 스터디 생성을 이메일로 알람을 받음

    private boolean studyCreatedByWeb;   // 스터디 생성을 웹으로 알람을 받음

    private boolean studyEnrollmentResultByEmail; // 스터디 가입 신청 결과를 이메일로 받음

    private boolean studyEnrollmentResultByWeb;   // 스터디 가입 신청 결과를 웹으로 받

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;
}
