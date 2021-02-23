package com.studyolleh.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import com.studyolleh.modules.tag.form.TagForm;
import com.studyolleh.modules.zone.form.ZoneForm;
import com.studyolleh.modules.tag.repository.TagRepository;
import com.studyolleh.modules.zone.repository.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.studyolleh.modules.account.controller.SettingController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired
    AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

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

    @WithAccount(testName)
    @DisplayName("지역 수 폼")
    @Test
    void update_zone_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(ZONES_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @Transactional
    @WithAccount(testName)
    @DisplayName("지역 추가 - 정상 입력")
    @Test
    void add_zone() throws Exception{
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account account = accountRepository.findByNickname(testName);
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(accountService.getZones(account).contains(zone));
    }

    @Transactional
    @WithAccount(testName)
    @DisplayName("지역 추가 - 비정상 입력")
    @Test
    void add_zone_with_wrong_input() throws Exception{
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString() + "wrong_value");
        mockMvc.perform(post(ROOT + SETTINGS + ZONES_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isBadRequest());

        Account account = accountRepository.findByNickname(testName);
        assertTrue(account.getZones().stream().count() == 0);
    }

    @Transactional
    @WithAccount(testName)
    @DisplayName("지역 삭제 - 정상 입력")
    @Test
    void remove_zone() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        accountService.addZone(account, testZone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES_URL + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertFalse(account.getZones().contains(zone));
    }

    @WithAccount(testName)
    @DisplayName("태그 수정 폼")
    @Test
    void update_tag_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(TAGS_VIEW_NAME))
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
        mockMvc.perform(post(ROOT + SETTINGS + TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());


        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account account = accountRepository.findByNickname(testName);
        assertTrue(accountService.getTags(account).contains(newTag));
    }

    @Transactional
    @WithAccount(testName)
    @DisplayName("태그 삭제")
    @Test
    void remove_tag() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, newTag);

        assertTrue(accountService.getTags(account).contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post(ROOT + SETTINGS + TAGS_URL + "/remove")
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
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().attributeExists("account"));
    }

    @WithAccount(testName)
    @DisplayName("닉네임 수정 - 입력값 정상")
    @Test
    void update_account() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT_URL)
                .param("nickname", "newnickname")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + ACCOUNT_URL))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname("newnickname"));
    }

    @WithAccount(testName)
    @DisplayName("닉네임 수정 - 입력값 오류")
    @Test
    void update_account_with_error() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT_URL)
                .param("nickname", "ERROR_NICKNAME")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(ACCOUNT_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));

        assertNotNull(accountRepository.findByNickname(testName));
    }

    @WithAccount(testName)
    @DisplayName("프로필 수정 폼")
    @Test
    void udpate_profile_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount(testName)
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void update_profile_with_correct_input() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account byNickname = accountRepository.findByNickname(testName);
        assertEquals(byNickname.getBio(), bio);
    }

    @WithAccount(testName)
    @DisplayName("프로필 수정하기 - 입력값 오류")
    @Test
    void update_profile_with_wrong_input() throws Exception {

        String bio = "50글자 이상의 긴 입력을 짧은 소개로 넣었을 경우, 50글자 이상의 긴 입력을 짧은 소개로 넣었을 경우, 50글자 이상의 긴 입력을 짧은 소개로 넣었을 경우";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE_URL)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_VIEW_NAME))
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
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount(testName)
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD_URL)
                .param("newPassword", "asdfasdf")
                .param("newPasswordConfirm", "asdfasdf")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account byNickname = accountRepository.findByNickname(testName);
        assertTrue(passwordEncoder.matches("asdfasdf", byNickname.getPassword()));
    }
}