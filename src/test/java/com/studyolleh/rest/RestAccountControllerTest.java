package com.studyolleh.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.infra.RestDocIndentConfig;
import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.account.AccountRepository;
import com.studyolleh.modules.account.AccountService;
import com.studyolleh.restapi.account.dto.LoginRequestDto;
import com.studyolleh.restapi.account.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
@AutoConfigureRestDocs
@Import(RestDocIndentConfig.class)
public class RestAccountControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;

    @DisplayName("로그인 요청 - 성공")
    @Test
    void loginSuccess() throws Exception{
        SignUpRequestDto signUpRequestDto = SignUpRequestDto.builder()
                .nickname("blupine")
                .email("test@email.com")
                .password("asdfasdf")
                .build();
        accountService.processNewAccountWithDto(signUpRequestDto);

        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .username("blupine")
                .password("asdfasdf")
                .build();

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("authToken").hasJsonPath())
                .andExpect(jsonPath("_links").hasJsonPath())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.user-profile").hasJsonPath())
                .andExpect(jsonPath("_links.profile").hasJsonPath())
                .andDo(document("user-login-success",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("user-profile").description("link to login user's profile")
                        ),
                        requestFields(
                                fieldWithPath("username").description("Email or Nickname to login"),
                                fieldWithPath("password").description("Password")
                        ),
                        responseFields(
                                fieldWithPath("authToken").description("JWT Token can be used to authenticate user to server."),
                                fieldWithPath("_links.self.href").description("self link"),
                                fieldWithPath("_links.user-profile.href").description("user's profile link"),
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

        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .username("blupine")
                .password("asdfasdf+fail")
                .build();

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

}
