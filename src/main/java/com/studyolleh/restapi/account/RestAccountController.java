package com.studyolleh.restapi.account;

import com.studyolleh.modules.account.authentication.CurrentAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.restapi.account.dto.*;
import com.studyolleh.restapi.account.validator.SignUpRequestDtoValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestAccountController {

    private final SignUpRequestDtoValidator signUpRequestDtoValidator;
    private final AccountService accountService;
    private final RestAccountService loginService;
    private final ModelMapper modelMapper;
    private WebMvcLinkBuilder webMvcLinkBuilder = WebMvcLinkBuilder.linkTo(RestAccountController.class);
    private URI uri = webMvcLinkBuilder.toUri();

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequestDto loginRequestDto, Errors errors) {
        Optional<AccountDto> optional = loginService.login(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        EntityModel<LoginResultDto> entityModel;

        if (optional.isPresent()) {
            String authToken = loginService.createAuthToken(optional.get());
            entityModel = EntityModel.of(new LoginResultDto(authToken));
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("login").withSelfRel());
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("user-profile").withRel("user-profile"));
            entityModel.add(Link.of("/docs/index.html#user-login-success").withRel("profile"));
            return ResponseEntity.accepted().body(entityModel);
        } else {
            // TODO : Should handle some other exception cases.
            throw new BadCredentialsException("Unknown error");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody @Valid SignUpRequestDto signUpRequestDto, Errors errors) {

        signUpRequestDtoValidator.validate(signUpRequestDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        Account account = accountService.processNewAccountWithDto(signUpRequestDto);

        SignUpResultDto signUpResultDto = modelMapper.map(account, SignUpResultDto.class);
        EntityModel<SignUpResultDto> entityModel = EntityModel.of(signUpResultDto);
        entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("signup").withSelfRel());
        entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("login").withRel("login"));
        entityModel.add(Link.of("/docs/index.html#user-signup").withRel("profile"));
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

    // TODO : 이메일 인증 링크 가져오기

    // TODO : 

}
