package com.studyolleh.modules.account;

import com.studyolleh.modules.account.authentication.UserAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String eamilOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(eamilOrNickname);
        if(account == null){
            account = accountRepository.findByNickname(eamilOrNickname);
        }
        if(account == null){
            return null;
        }
        return new UserAccount(account);
    }
}
