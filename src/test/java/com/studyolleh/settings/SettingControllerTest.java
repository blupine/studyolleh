package com.studyolleh.settings;

import com.studyolleh.WithAccount;
import com.studyolleh.account.AccountRepository;
import com.studyolleh.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingControllerTest {

    static final String testName = "testName";

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;


    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount(testName)
    @DisplayName("프로필 수정 폼")
    @Test
    void udpate_profile_form() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount(testName)
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void update_profile_with_correct_input() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account byNickname = accountRepository.findByNickname(testName);
        assertEquals(byNickname.getBio(), bio);
    }

    @WithAccount(testName)
    @DisplayName("프로필 수정하기 - 입력값 오류")
    @Test
    void update_profile_with_wrong_input() throws Exception {

        String bio = "50글자 이상의 긴 입력을 짧은 소개로 넣었을 경우, 50글자 이상의 긴 입력을 짧은 소개로 넣었을 경우, 50글자 이상의 긴 입력을 짧은 소개로 넣었을 경우";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());
        Account byNickname = accountRepository.findByNickname(testName);
        assertNull(byNickname.getBio());
    }

    @WithAccount(testName)
    @DisplayName("패스워드 수정폼")
    @Test
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount(testName)
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword() throws Exception {
        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "asdfasdf")
                .param("newPasswordConfirm", "asdfasdf")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account byNickname = accountRepository.findByNickname(testName);
        assertTrue(passwordEncoder.matches("asdfasdf", byNickname.getPassword()));
    }
}