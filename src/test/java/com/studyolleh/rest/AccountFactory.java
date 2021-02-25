package com.studyolleh.rest;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.restapi.account.dto.SignUpRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountFactory {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    public Account createAccount(String name) {

        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .nickname(name)
                .password(name)
                .email(name + "@zzzz")
                .build();
        return accountService.processNewAccountWithDto(signUpRequestDto);
    }
}