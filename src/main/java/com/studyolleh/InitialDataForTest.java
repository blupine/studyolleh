package com.studyolleh;

import com.studyolleh.account.AccountController;
import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.AccountService;
import com.studyolleh.account.form.SignUpForm;
import com.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class InitialDataForTest {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    @Bean
    public void init() {
//        Account account = Account.builder()
//                .email("skwint11@gmail.com")
//                .nickname("blupine")
//                .password(passwordEncoder.encode("asdfasdf"))
//                .studyCreatedByWeb(true)
//                .studyEnrollmentResultByWeb(true)
//                .studyUpdatedByWeb(true)
//                .build();
//
//        account.generateEmailCheckToken();
//        account.completeSignUp();
//        accountRepository.save(account);
//
//        Account byNickname = accountRepository.findByNickname("blupine");
//        System.out.println("byNickname.getNickname() = " + byNickname.getNickname());
    }

}
