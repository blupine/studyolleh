package com.studyolleh.study;

import com.studyolleh.WithAccount;
import com.studyolleh.account.AccountRepository;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class StudyControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected StudyService studyService;
    @Autowired protected StudyRepository studyRepository;
    @Autowired protected AccountRepository accountRepository;

    protected static final String testName = "testname";

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
        assertTrue(study.getManagers().contains(account));
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
        Account account = createAccount("tempUser");
        Study study = createStudy("test-study", account);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/join"))
                .andExpect(status().isForbidden());

        Account account2 = accountRepository.findByNickname(testName);
        assertFalse(study.getMembers().contains(account2));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 가입 - 성공")
    void joinStudy() throws Exception {
        Account account = createAccount("tempUser");
        Study study = createStudy("test-study", account);
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
        Account account = createAccount("tempUser");
        Study study = createStudy("test-study", account);
        study.setPublished(true);
        study.setRecruiting(true);

        Account account1 = accountRepository.findByNickname(testName);
        studyService.addMember(study, account1);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/members"));

        assertFalse(study.getMembers().contains(account1));
    }

    protected Account createAccount(String nickname) {
        Account account = new Account();
        account.setNickname(nickname);
        account.setEmail(nickname + "@email.com");
        accountRepository.save(account);

        return account;
    }

    protected Study createStudy(String path, Account account) {
        Study study = new Study();
        study.setPath(path);

        studyService.createNewStudy(study, account);
        return study;
    }
}