package com.studyolleh.restapi.account.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignUpResultDto {

    private String nickname;

    private String email;

    private boolean emailVerified;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String url;

}
