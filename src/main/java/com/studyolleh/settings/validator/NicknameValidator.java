package com.studyolleh.settings.validator;

import com.studyolleh.account.AccountRepository;
import com.studyolleh.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(NicknameForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;
        if(accountRepository.existsByNickname(nicknameForm.getNickname())){
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다.");
        }
    }
}
