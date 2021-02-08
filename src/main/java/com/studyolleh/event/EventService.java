package com.studyolleh.event;

import com.studyolleh.domain.Account;
import com.studyolleh.domain.Event;
import com.studyolleh.domain.Study;
import com.studyolleh.event.form.EventForm;
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
        // TODO : 모집 방식이 선착순이고, 모집 인원이 늘어났을 경우에 늘어난 모집 인원만큼 지원자들의 참가 신청 상태를 확정 상태로 변경해야 함
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }
}
