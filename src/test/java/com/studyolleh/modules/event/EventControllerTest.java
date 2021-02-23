package com.studyolleh.modules.event;

import com.studyolleh.infra.AbstractContainerBaseTest;
import com.studyolleh.infra.MockMvcTest;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.AccountFactory;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.WithAccount;
import com.studyolleh.modules.event.domain.Enrollment;
import com.studyolleh.modules.event.domain.Event;
import com.studyolleh.modules.event.domain.EventType;
import com.studyolleh.modules.event.repository.EnrollmentRepository;
import com.studyolleh.modules.event.repository.EventRepository;
import com.studyolleh.modules.event.service.EventService;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountFactory accountFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired
    EventService eventService;
    @Autowired EventRepository eventRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @WithAccount(testName)
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    void 선착순_모임에_참가신청_자동수락() throws Exception {
        Account test1 = accountFactory.createAccount("test1");
        Study study = studyFactory.createStudy("test-path", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);

        mockMvc.perform(post("/study/" + study.getEncodedPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/events/" + event.getId()));

        Account account = accountRepository.findByNickname(testName);
        assertEnrollAccepted(account, event);
    }

    @Test
    @WithAccount(testName)
    @DisplayName("선착순 모임에 참가 신청 - 대기중(이미 인원이 다 찼을 경우)")
    void 선착순_모임에_참가신청_대기중_인원이_이미_다_참() throws Exception{
        // given
        Account test1 = accountFactory.createAccount("test1");
        Study study = studyFactory.createStudy("test-path", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);

        Account test2 = accountFactory.createAccount("test2");
        Account test3 = accountFactory.createAccount("test3");
        eventService.newEnrollment(event, test2);
        eventService.newEnrollment(event, test3);

        // when & then
        mockMvc.perform(post("/study/" + study.getEncodedPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/events/" + event.getId()));

        Account account = accountRepository.findByNickname(testName);
        assertEnrollNotAccepted(account, event);
    }

    @Test
    @WithAccount(testName)
    @DisplayName("선착순 모임의 참가신청 확정자가 신청을 취소할 경우 - 다음 대기자를 자동으로 확정")
    void 선착순_모임의_참가신청_확정자가_신청을_취소할_경우() throws Exception {
        // given
        Account account = accountRepository.findByNickname(testName);
        Account test1 = accountFactory.createAccount("test1");
        Account test2 = accountFactory.createAccount("test2");
        Study study = studyFactory.createStudy("test-path", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);

        eventService.newEnrollment(event, test1);
        eventService.newEnrollment(event, account);
        eventService.newEnrollment(event, test2);

        assertEnrollAccepted(test1, event);
        assertEnrollAccepted(account, event);
        assertEnrollNotAccepted(test2, event);

        // when & then
        mockMvc.perform(post("/study/" + study.getEncodedPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/events/" + event.getId()));


        assertEnrollAccepted(test1, event);
        assertEnrollAccepted(test2, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, account));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자는 변함이 없음")
    void 참가신청_비확정자가_취소할경우_변함없음() throws Exception {
        // given
        Account account = accountRepository.findByNickname(testName);
        Account test1 = accountFactory.createAccount("test1");
        Account test2 = accountFactory.createAccount("test2");
        Study study = studyFactory.createStudy("test-path", test1);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, test1);

        eventService.newEnrollment(event, test1);
        eventService.newEnrollment(event, test2);
        eventService.newEnrollment(event, account);

        assertEnrollAccepted(test1, event);
        assertEnrollAccepted(test2, event);
        assertEnrollNotAccepted(account, event);

        // when & then
        mockMvc.perform(post("/study/" + study.getEncodedPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/events/" + event.getId()));

        assertEnrollAccepted(test1, event);
        assertEnrollAccepted(test2, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, account));
    }

    @Test
    @WithAccount(testName)
    @DisplayName("관리자 확인 모임에 참가신청 - 대기중")
    void 관리자_확인_모임에_참가신청_대기중() throws Exception {
        Account account = accountRepository.findByNickname(testName);
        Account test1 = accountFactory.createAccount("test1");
        Study study = studyFactory.createStudy("test-path", test1);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, test1);

        // when & then
        mockMvc.perform(post("/study/" + study.getEncodedPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getEncodedPath() + "/events/" + event.getId()));

        assertEnrollNotAccepted(account, event);
    }

    private void assertEnrollAccepted(Account account, Event event) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        assertTrue(enrollment.isAccepted());
    }

    private void assertEnrollNotAccepted(Account account, Event event) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        assertFalse(enrollment.isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setTitle(eventTitle);
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }
}