package com.studyolleh.restapi.account.dto;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class CheckEmailResponseDto {
    @NonNull private Long memberCount;
    @NonNull private String nickname;
}
