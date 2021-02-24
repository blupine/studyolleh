package com.studyolleh.modules.study;

import com.studyolleh.modules.account.AccountFactory;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.authentication.UserAccount;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.repository.StudyAccountRepository;
import com.studyolleh.modules.study.repository.StudyRepository;
import com.studyolleh.modules.study.service.StudyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudyTest {

    Study study;
    Account account, manager;
    UserAccount userAccount;

    @Autowired StudyService studyService;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyAccountRepository studyAccountRepository;

    @BeforeEach
    void beforeEach() {

        manager = accountFactory.createAccount("studyManager");
        account = accountFactory.createAccount("testuser");
        study = studyFactory.createStudy("test", manager);

        userAccount = new UserAccount(account);
    }

    @AfterEach
    void afterEach() {
        studyRepository.deleteAll();
        accountRepository.deleteAll();
        accountRepository.deleteAll();
        studyAccountRepository.deleteAll();
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이고, 이미 멤버나 매니저가 아닌 경우 가입 가능 확인")
    @Test
    void isJoinable() {
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinable(userAccount));
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이더라도, 스터디 관리자는 스터디 가입이 불필요하다.")
    @Test
    void isJoinable_false_for_manager() {
        study.setPublished(true);
        study.setRecruiting(true);
        studyService.addManager(study, account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디를 공개했고 인원 모집 중이더라도, 스터디 멤버는 스터디 재가입이 불필요하다.")
    @Test
    void isJoinable_false_for_member() {
        study.setPublished(true);
        study.setRecruiting(true);
        studyService.addMember(study, account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디가 비공개거나 인원 모집 중이 아니면 스터디 가입이 불가능하다.")
    @Test
    void isJoinable_false_for_non_recruiting_study() {
        study.setPublished(true);
        study.setRecruiting(false);

        assertFalse(study.isJoinable(userAccount));

        study.setPublished(false);
        study.setRecruiting(true);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디 관리자인지 확인")
    @Test
    void isManager() {
        study.setPublished(true);
        study.setRecruiting(true);
        studyService.addManager(study, account);
        assertTrue(studyService.getStudyManagers(study).contains(account));
    }

    @DisplayName("스터디 멤버인지 확인")
    @Test
    void isMember() {
        study.setPublished(true);
        study.setRecruiting(true);
        studyService.addMember(study, account);
        assertTrue(studyService.getStudyMembers(study).contains(account));
    }

}