package com.studyolleh.modules.account;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountFactory {

    @Autowired
    AccountRepository accountRepository;

    public Account createAccount(String name) {
        Account account = new Account();
        account.setNickname(name);
        account.setEmail(name + "@email.com");
        account.setPassword(name);
        accountRepository.save(account);
        return account;
    }
}
