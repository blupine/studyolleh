package com.studyolleh.restapi.account.validator;

import com.studyolleh.modules.account.AccountRepository;
import com.studyolleh.restapi.account.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpRequestDtoValidator{

    private final AccountRepository accountRepository;

    public void validate(SignUpRequestDto signUpRequestDto, Errors errors) {
        if (accountRepository.existsByEmail(signUpRequestDto.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpRequestDto.getEmail()}, "이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(signUpRequestDto.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpRequestDto.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
    }
}
