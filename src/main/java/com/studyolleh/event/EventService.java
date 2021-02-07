package com.studyolleh.event;

import com.studyolleh.domain.Account;
import com.studyolleh.domain.Event;
import com.studyolleh.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

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
}
