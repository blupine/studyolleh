package com.studyolleh.modules.study;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.authentication.UserAccount;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.service.StudyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudyTest {

    Study study;
    Account account;
    UserAccount userAccount;

    @Autowired
    StudyService studyService;

    @BeforeEach
    void beforeEach() {
        study = new Study();
        account = new Account();
        account.setNickname("testName");
        account.setPassword("12341234");
        userAccount = new UserAccount(account);


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
        studyService.addManager(study, account);
        assertTrue(study.isManager(userAccount));
    }

    @DisplayName("스터디 멤버인지 확인")
    @Test
    void isMember() {

        studyService.addMember(study, account);
        assertTrue(study.isMember(userAccount));
    }

}