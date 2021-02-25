package com.studyolleh.restapi.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class LoginResultDto {
    @NonNull private String authToken;
}
