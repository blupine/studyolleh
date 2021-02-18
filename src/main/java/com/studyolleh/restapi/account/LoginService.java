package com.studyolleh.restapi.account;

import com.studyolleh.infra.config.JwtTokenProvider;
import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.account.UserAccount;
import com.studyolleh.restapi.account.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final static long LOGIN_RETENTION_MINUTES = 30;
    private final ModelMapper modelMapper;
    /**
     * @param username : nickname or email
     * @param password
     * @return
     */
    public Optional<AccountDto> login(String username, String password) {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserAccount principal = (UserAccount) authentication.getPrincipal();
        Account account = principal.getAccount();

        AccountDto accountDto = modelMapper.map(account, AccountDto.class);

        return Optional.ofNullable(accountDto);
    }

    public String createAuthToken(AccountDto accountDto) {
        return jwtTokenProvider.createToken(accountDto.getNickname());
    }
}
