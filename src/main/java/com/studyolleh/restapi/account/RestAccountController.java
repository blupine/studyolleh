package com.studyolleh.restapi.account;

import com.studyolleh.restapi.account.dto.AccountDto;
import com.studyolleh.restapi.account.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestAccountController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDto loginRequestDto) {
        Optional<AccountDto> optional = loginService.login(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        if (optional.isPresent()) {
            String authToken = loginService.createAuthToken(optional.get());
            return new ResponseEntity<>(authToken, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


}
