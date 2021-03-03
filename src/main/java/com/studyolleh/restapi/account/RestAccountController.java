package com.studyolleh.restapi.account;

import com.studyolleh.modules.account.authentication.CurrentAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.restapi.account.dto.*;
import com.studyolleh.restapi.account.validator.SignUpRequestDtoValidator;
import com.studyolleh.restapi.exception.ErrorResponse;
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
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid LoginRequestDto loginRequestDto, Errors errors) {
        Optional<AccountDto> optional = loginService.login(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        EntityModel<LoginResultDto> entityModel;

        if (optional.isPresent()) {
            String authToken = loginService.createAuthToken(optional.get());
            entityModel = EntityModel.of(LoginResultDto.of(authToken));
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("login").withSelfRel());
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("my-info").withRel("my-info"));
            entityModel.add(Link.of("/docs/index.html#user-login-success").withRel("profile"));
            return ResponseEntity.accepted().body(entityModel);
        } else {
            // TODO : Should handle some other exception cases.
            throw new BadCredentialsException("Unknown error");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody @Valid SignUpRequestDto signUpRequestDto, Errors errors) {

        // TODO : Valid 로직 수정 필요 -> errors 바로 반환하는걸 고쳐야 할듯
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
        return ResponseEntity.created(createUri("signup")).body(entityModel);
    }

    @GetMapping("/my-info")
    public ResponseEntity getUserInfo(@CurrentAccount Account account) {
        EntityModel<AccountDto> entityModel;
        if (account != null) {
            AccountDto accountDto = modelMapper.map(account, AccountDto.class);
            entityModel = EntityModel.of(accountDto);
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("my-info").withSelfRel());
            // TODO : need to add next link
            return ResponseEntity.created(createUri("my-info")).body(entityModel);
        }
        return ResponseEntity.notFound().build();
    }

    // TODO : 이메일 인증하기
    @GetMapping("/check-email-token")
    public ResponseEntity checkEmailToken(String token, String email) {
        Account account = accountRepository.findByEmail(email);

        if (account != null && account.isValidToken(token)) {
            accountService.completeSignUp(account);
            CheckEmailResponseDto dto = CheckEmailResponseDto.of(accountRepository.count(), account.getNickname());
            EntityModel entityModel = EntityModel.of(dto);
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("check-email-token").withSelfRel());
            entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("my-info").withRel("my-info"));
            entityModel.add(Link.of("/docs/index.html#check-email-token").withRel("profile"));
            return ResponseEntity.accepted().body(entityModel);
        }
        return ResponseEntity.notFound().build();
    }

    private static URI createUri(String resource) {
        WebMvcLinkBuilder webMvcLinkBuilder = WebMvcLinkBuilder.linkTo(RestAccountController.class).slash(resource);
        return webMvcLinkBuilder.toUri();
    }

}
