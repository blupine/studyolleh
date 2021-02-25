package com.studyolleh.restapi.account.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class LoginRequestDto {
    @NonNull private String username;
    @NonNull private String password;
}
