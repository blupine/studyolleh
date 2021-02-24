package com.studyolleh.modules.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.modules.account.AccountFactory;
import com.studyolleh.modules.account.domain.TagItem;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.WithAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.domain.StudyTagItem;
import com.studyolleh.modules.tag.form.TagForm;
import com.studyolleh.modules.zone.domain.Zone;
import com.studyolleh.modules.zone.form.ZoneForm;
import com.studyolleh.modules.zone.repository.ZoneRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired ZoneRepository zoneRepository;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired ObjectMapper objectMapper;

    private Zone testZone = Zone.builder().city("city").localNameOfCity("localName").province("province").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @Test
    @WithAccount(testName)
    @DisplayName("스터디 소개 수정 폼 조회 - 실패(권한 없는 사용자)")
    void updateDescriptionForm_fail() throws Exception {
        Account account = accountFactory.createAccount("tempUser");
        Study study = studyFactory.createStudy("test-study", account);

        mockMvc.perform(get("/study/" + study.getEncodedPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
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


    @Test
    @WithAccount(testName)
    @DisplayName("태그 추가 - 성공")
    void addTag_success() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        Study study = studyFactory.createStudy("test-study", account);
        String url = "/study/" + study.getEncodedPath() + "/settings/tags/add";

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("test");
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());
        Set<String> tags = study.getTags().stream().map(studyTagItem -> studyTagItem.getTag().getTitle()).collect(Collectors.toSet());

        Assertions.assertTrue(tags.contains("test"));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("지역 추가 - 성공")
    void addZone_success() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        Study study = studyFactory.createStudy("test-study", account);
        String url = "/study/" + study.getEncodedPath() + "/settings/zones/add";

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());
        Set<String> zones = study.getZones().stream().map(studyZoneItem -> studyZoneItem.getZone().toString()).collect(Collectors.toSet());

        Assertions.assertTrue(zones.contains(testZone.toString()));
    }

}