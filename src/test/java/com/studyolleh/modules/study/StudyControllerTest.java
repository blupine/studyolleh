package com.studyolleh.modules.study;

import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.modules.account.AccountFactory;
import com.studyolleh.modules.account.WithAccount;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.domain.Account;

import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.repository.StudyRepository;
import com.studyolleh.modules.study.service.StudyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class StudyControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    StudyService studyService;
    @Autowired
    StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 개설 폼 조회")
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 개설 - 완료")
    void createStudy_success() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/test-path"));

        Study study = studyRepository.findByPath("test-path");
        assertNotNull(study);
        Account account = accountRepository.findByNickname(testName);
        assertTrue(studyService.getStudyMembers(study).contains(account));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 개설 - 실패")
    void createStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "wrong path")
                .param("title", "study title")
                .param("shortDescription", "short description of a study")
                .param("fullDescription", "full description of a study")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(model().attributeExists("account"));

        Study study = studyRepository.findByPath("test-path");
        assertNull(study);
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("full description");

        Account account = accountRepository.findByNickname(testName);
        studyService.createNewStudy(study, account);

        mockMvc.perform(get("/study/test-path"))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 멤버 조회 폼")
    void studyMemberForm() throws Exception {
        // given
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("full description");

        Account account = accountRepository.findByNickname(testName);
        studyService.createNewStudy(study, account);

        // when
        mockMvc.perform(get("/study/test-path/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/members"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

        // then
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 가입 - 실패(미공개, 모집중이지 않은 스터디)")
    void joinStudy_fail() throws Exception {
        Account account = accountFactory.createAccount("tempUser");
        Study study = studyFactory.createStudy("test-study", account);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/join"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        Account account2 = accountRepository.findByNickname(testName);
        assertFalse(study.getMembers().contains(account2));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 가입 - 성공")
    void joinStudy() throws Exception {
        Account account = accountFactory.createAccount("tempUser");
        Study study = studyFactory.createStudy("test-study", account);
        study.setPublished(true);
        study.setRecruiting(true);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/members"));

        Account account2 = accountRepository.findByNickname(testName);
        assertTrue(study.getMembers().contains(account2));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 탈퇴")
    void leaveStudy() throws Exception {
        Account account = accountFactory.createAccount("tempUser");
        Study study = studyFactory.createStudy("test-study", account);
        study.setPublished(true);
        study.setRecruiting(true);

        Account account1 = accountRepository.findByNickname(testName);
        studyService.addMember(study, account1);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/members"));

        assertFalse(study.getMembers().contains(account1));
    }
}