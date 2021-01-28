package com.studyolleh;

import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.AccountService;
import com.studyolleh.account.SignUpForm;
import com.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class InitialDataForTest {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    @Bean
    public void init() {
        System.out.println("InitialDataForTest.init");
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("blupine");
        signUpForm.setPassword("asdfasdf");
        signUpForm.setEmail("skwint11@gmail.com");
        Account account = accountService.processNewAccount(signUpForm);
        account.completeSignUp();

        Account byNickname = accountRepository.findByNickname("blupine");
        System.out.println("byNickname.getNickname() = " + byNickname.getNickname());
    }

}
