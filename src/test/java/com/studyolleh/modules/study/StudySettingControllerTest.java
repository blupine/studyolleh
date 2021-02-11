package com.studyolleh.modules.study;

import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.modules.account.AccountFactory;
import com.studyolleh.modules.account.AccountRepository;
import com.studyolleh.modules.account.WithAccount;
import com.studyolleh.modules.account.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 소개 수정 폼 조회 - 실패(권한 없는 사용자)")
    void updateDescriptionForm_fail() throws Exception {
        Account account = accountFactory.createAccount("tempUser");
        Study study = studyFactory.createStudy("test-study", account);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    void updateDescriptionForm() throws Exception{
        Account account = accountRepository.findByNickname(testName);
        Study study = studyFactory.createStudy("test-study", account);
        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("studyDescriptionForm"));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 소개 수정 - 성공")
    void updateDescription() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        Study study = studyFactory.createStudy("test-study", account);
        String url = "/study/" + study.getEncodedPath() + "/settings/description";
        mockMvc.perform(post(url)
                .param("shortDescription", "short description")
                .param("fullDescription", "full descirpition")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(url))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 소개 수정 - 실패")
    void updateDescription_fail() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        Study study = studyFactory.createStudy("test-study", account);
        String url = "/study/" + study.getEncodedPath() + "/settings/description";
        mockMvc.perform(post(url)
                .param("shortDescription", "")
                .param("fullDescription", "full descirpition")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }
}