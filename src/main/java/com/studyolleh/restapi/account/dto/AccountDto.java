package com.studyolleh.restapi.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class AccountDto {

    private Long id;

    private String email;

    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    private String profileImage;

    private boolean studyCreatedByEmail; // 스터디 생성을 이메일로 알람을 받음

    private boolean studyCreatedByWeb = true;   // 스터디 생성을 웹으로 알람을 받음

    private boolean studyEnrollmentResultByEmail; // 스터디 가입 신청 결과를 이메일로 받음

    private boolean studyEnrollmentResultByWeb = true;   // 스터디 가입 신청 결과를 웹으로 받

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;
}