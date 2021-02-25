package com.studyolleh.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.infra.RestDocIndentConfig;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.restapi.account.dto.LoginRequestDto;
import com.studyolleh.restapi.account.dto.SignUpRequestDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@AutoConfigureRestDocs
@Import(RestDocIndentConfig.class)
public class RestAccountControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;
    @Autowired AccountFactory accountFactory;

    @DisplayName("로그인 요청 - 성공")
    @Test
    void loginSuccess() throws Exception{
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .nickname("blupine")
                .email("test@email.com")
                .password("asdfasdf")
                .build();
        accountService.processNewAccountWithDto(signUpRequestDto);

        LoginRequestDto loginRequestDto = LoginRequestDto.of("blupine", "asdfasdf");
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("authToken").hasJsonPath())
                .andExpect(jsonPath("_links").hasJsonPath())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.my-info").hasJsonPath())
                .andExpect(jsonPath("_links.profile").hasJsonPath())
                .andDo(document("user-login-success",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("my-info").description("link to login user's profile"),
                                linkWithRel("profile").description("link to api docs")
                        ),
                        requestFields(
                                fieldWithPath("username").description("Email or Nickname to login"),
                                fieldWithPath("password").description("Password")
                        ),
                        responseFields(
                                fieldWithPath("authToken").description("JWT Token can be used to authenticate user to server."),
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.my-info.href").description("user's profile link"),
                                fieldWithPath("_links.profile.href").description("api docs link")
                        )
                ));
    }

    @DisplayName("로그인 요청 - 실패 (bad credential)")
    @Test
    void loginFail() throws Exception{
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .nickname("blupine")
                .email("test@email.com")
                .password("asdfasdf")
                .build();
        accountService.processNewAccountWithDto(signUpRequestDto);

        LoginRequestDto loginRequestDto = LoginRequestDto.of("blupine", "asdfasdf+fail");
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("httpStatus").hasJsonPath())
                .andExpect(jsonPath("message").hasJsonPath())
                .andExpect(jsonPath("_links").hasJsonPath())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.signup").hasJsonPath())
                .andExpect(jsonPath("_links.profile").hasJsonPath())
                .andDo(document("user-login-fail",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("signup").description("link to signup"),
                                linkWithRel("profile").description("link to api docs")
                        ),
                        requestFields(
                                fieldWithPath("username").description("Email or Nickname to login"),
                                fieldWithPath("password").description("Password")
                        ),
                        responseFields(
                                fieldWithPath("httpStatus").description("HTTP Status code"),
                                fieldWithPath("message").description("Error message"),
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.signup.href").description("signup link"),
                                fieldWithPath("_links.profile.href").description("api docs link")
                        )
                ));
    }

    @DisplayName("회원가입 - 정상 입력")
    @Test
    void signupTest() throws Exception {
        /* given */
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .nickname("blupine")
                .email("test@email.com")
                .password("asdfasdf")
                .build();

        /* when & then */
        mockMvc.perform(post("/api/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(signUpRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("nickname").hasJsonPath())
                .andExpect(jsonPath("email").hasJsonPath())
                .andExpect(jsonPath("emailVerified").hasJsonPath())
                .andExpect(jsonPath("emailCheckTokenGeneratedAt").hasJsonPath())
                .andExpect(jsonPath("joinedAt").hasJsonPath())
                .andExpect(jsonPath("url").hasJsonPath())
                .andExpect(jsonPath("_links").hasJsonPath())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.login").hasJsonPath())
                .andExpect(jsonPath("_links.profile").hasJsonPath())
                .andDo(document("user-signup",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("login").description("link to login"),
                                linkWithRel("profile").description("link to api docs")
                                ),
                        requestFields(
                                fieldWithPath("nickname").description("ID"),
                                fieldWithPath("email").description("Email"),
                                fieldWithPath("password").description("Password")
                        ),
                        responseFields(
                                fieldWithPath("nickname").description("ID"),
                                fieldWithPath("email").description("Email"),
                                fieldWithPath("emailVerified").description("Email verified?"),
                                fieldWithPath("emailCheckTokenGeneratedAt").description("Email verified time"),
                                fieldWithPath("joinedAt").description("Signup time"),
                                fieldWithPath("url").description("Profile URL"),
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.login.href").description("login link"),
                                fieldWithPath("_links.profile.href").description("api docs link")
                        )));

        /* then */
        Account accountAfter = accountRepository.findByNickname("blupine");
        Assertions.assertNotNull(accountAfter);
    }

    @DisplayName("이메일 인증 - 성공")
    @Test
    void checkEmailSuccess() throws Exception {
        Account account = accountFactory.createAccount("testname");

        mockMvc.perform(get("/api/check-email-token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .param("token", account.getEmailCheckToken())
                .param("email", account.getEmail()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("memberCount").hasJsonPath())
                .andExpect(jsonPath("nickname").hasJsonPath())
                .andExpect(jsonPath("_links").hasJsonPath())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.my-info").hasJsonPath())
                .andExpect(jsonPath("_links.profile").hasJsonPath())
                .andDo(print())
                .andDo(document("check-email-token",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("my-info").description("link to login user info"),
                                linkWithRel("profile").description("link to api docs")
                        ),
                        requestParameters(
                                parameterWithName("token").description("Email authentication token send from server through email."),
                                parameterWithName("email").description("Email address")
                        ),
                        responseFields(
                                fieldWithPath("memberCount").description("Total Member count"),
                                fieldWithPath("nickname").description("Authenticated user's nickname"),
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.my-info.href").description("user's profile link"),
                                fieldWithPath("_links.profile.href").description("api docs link")
                        )));

        Assertions.assertTrue(account.isEmailVerified());
    }
}
