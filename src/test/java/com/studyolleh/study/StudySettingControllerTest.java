package com.studyolleh.study;

import com.studyolleh.WithAccount;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Study;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequestMapping("/study/{path}/settings")
class StudySettingControllerTest extends StudyControllerTest{

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 소개 수정 폼 조회 - 실패(권한 없는 사용자)")
    void updateDescriptionForm_fail() throws Exception {
        Account account = createAccount("tempUser");
        Study study = createStudy("test-study", account);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }
}