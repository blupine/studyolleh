package com.studyolleh.event;

import com.studyolleh.domain.Event;
import com.studyolleh.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStudyOrderByStartDateTime(Study study);

}
