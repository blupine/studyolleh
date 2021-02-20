package com.studyolleh.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.account.AccountRepository;
import com.studyolleh.restapi.account.dto.LoginRequestDto;
import com.studyolleh.restapi.account.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class RestAccountControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountRepository accountRepository;

    @DisplayName("로그인 요청")
    @Test
    void loginTest() throws Exception{
        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .username("blupine")
                .password("asdfasdf")
                .build();

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("authToken").exists())
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
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE));

        /* then */
        Account accountAfter = accountRepository.findByNickname("blupine");
        Assertions.assertNotNull(accountAfter);
    }

}
