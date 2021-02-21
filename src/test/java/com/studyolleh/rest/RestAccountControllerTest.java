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

    @DisplayName("로그인 요청")
    @Test
    void loginTest() throws Exception{
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("authToken").hasJsonPath())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));
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
                                linkWithRel("docs").description("link to api docs")
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
                                fieldWithPath("_links.docs.href").description("api docs link")
                        )));

        /* then */
        Account accountAfter = accountRepository.findByNickname("blupine");
        Assertions.assertNotNull(accountAfter);
    }

}
