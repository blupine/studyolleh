package com.studyolleh.restapi.account;

import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.account.AccountService;
import com.studyolleh.modules.account.CurrentAccount;
import com.studyolleh.restapi.account.dto.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestAccountController {

    private final AccountService accountService;
    private final RestAccountService loginService;
    private final ModelMapper modelMapper;
    private WebMvcLinkBuilder webMvcLinkBuilder = WebMvcLinkBuilder.linkTo(RestAccountController.class);
    private URI uri = webMvcLinkBuilder.toUri();

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDto loginRequestDto) {
        Optional<AccountDto> optional = loginService.login(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        EntityModel<LoginResultDto> entityModel;

        if (optional.isPresent()) {
            String authToken = loginService.createAuthToken(optional.get());
            entityModel = EntityModel.of(new LoginResultDto(authToken));
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("login").withSelfRel());
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("my-info").withRel("user-info"));
        } else {
            entityModel = EntityModel.of(new LoginResultDto(null));
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("login").withSelfRel());
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("signup").withRel("signup"));
        }
        return ResponseEntity.created(uri).body(entityModel);
    }

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        Account account = accountService.processNewAccountWithDto(signUpRequestDto);

        SignUpResultDto signUpResultDto = modelMapper.map(account, SignUpResultDto.class);
        EntityModel<SignUpResultDto> entityModel = EntityModel.of(signUpResultDto);
        entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("signup").withSelfRel());
        entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("login").withRel("login"));
        return ResponseEntity.created(uri).body(entityModel);
    }

    @GetMapping("/my-info")
    public ResponseEntity getUserInfo(@CurrentAccount Account account) {
        EntityModel<AccountDto> entityModel;
        if (account != null) {
            AccountDto accountDto = modelMapper.map(account, AccountDto.class);
            entityModel = EntityModel.of(accountDto);
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("my-info").withSelfRel());
            // TODO : need to add next link
            return ResponseEntity.created(uri).body(entityModel);
        }
        return ResponseEntity.created(uri).body(null);
    }
}
