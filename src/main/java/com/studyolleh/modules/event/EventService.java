package com.studyolleh.modules.event;

import com.studyolleh.modules.domain.Account;
import com.studyolleh.modules.domain.Enrollment;
import com.studyolleh.modules.domain.Event;
import com.studyolleh.modules.domain.Study;
import com.studyolleh.modules.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }

    public Event findEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 모임입니다."));
    }

    public List<Event> findByStudyOrderByStartDateTime(Study study) {
        return eventRepository.findByStudyOrderByStartDateTime(study);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setAccount(account);
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment();
        }
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
    }

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
