package com.studyolleh.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.WithAccount;
import com.studyolleh.account.AccountRepository;
import com.studyolleh.account.AccountService;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Tag;
import com.studyolleh.settings.form.TagForm;
import com.studyolleh.tag.TagRepository;
import lombok.With;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired AccountService accountService;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount(testName)
    @DisplayName("태그 수정 폼")
    @Test
    void update_tag_form() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }


    @Transactional // getTags 시에 지연로딩 발생 -> 트랜잭션 어노테이션이 필요함
    @WithAccount(testName)
    @DisplayName("태그 추가")
    @Test
    void add_tag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post(SettingController.SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());


        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account account = accountRepository.findByNickname(testName);
        assertTrue(account.getTags().contains(newTag));
    }

    @Transactional
    @WithAccount(testName)
    @DisplayName("태그 삭제")
    @Test
    void remove_tag() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, newTag);

        assertTrue(account.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post(SettingController.SETTINGS_TAGS_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getTags().contains(newTag));

    }

    @WithAccount(testName)
    @DisplayName("닉네임 수정 폼")
    @Test
    void update_account_form() throws Exception {
        mockMvc.perform(get(SettingController.SETTINGS_ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount(testName)
    @DisplayName("닉네임 수정 - 입력값 정상")
    @Test
    void update_account() throws Exception {
        mockMvc.perform(post(SettingController.SETTINGS_ACCOUNT_URL)
                .param("nickname", "newnickname")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname("newnickname"));
    }

    @WithAccount(testName)
    @DisplayName("닉네임 수정 - 입력값 오류")
    @Test
    void update_account_with_error() throws Exception {
        mockMvc.perform(post(SettingController.SETTINGS_ACCOUNT_URL)
                .param("nickname", "ERROR_NICKNAME")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));

        assertNotNull(accountRepository.findByNickname(testName));
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