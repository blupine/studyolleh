package com.studyolleh.modules.main;

import com.studyolleh.modules.account.AccountRepository;
import com.studyolleh.modules.account.AccountService;
import com.studyolleh.modules.account.form.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountService accountService;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("testname");
        signUpForm.setEmail("testname@gmail.com");
        signUpForm.setPassword("testPassword");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @DisplayName("이메일 로그인")
    @Test
    void login_with_email() throws Exception{
        mockMvc.perform(post("/login")
                .param("username", "testname@gmail.com")
                .param("password", "testPassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("testname"));
    }

    @DisplayName("닉네임 로그인")
    @Test
    void login_with_nickname() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "testname")
                .param("password", "testPassword")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("testname"));
    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception{
        mockMvc.perform(post("/login")
                .param("username", "asdfasdf")
                .param("password", "asdfasdf")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @WithMockUser
    @DisplayName("로그아웃")
    @Test
    void logout() throws Exception{
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}