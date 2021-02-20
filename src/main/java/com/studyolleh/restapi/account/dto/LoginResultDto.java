package com.studyolleh.restapi.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResultDto {
    private String authToken;
}
