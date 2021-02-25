package com.studyolleh.modules.event.repository;

import com.studyolleh.modules.event.domain.Enrollment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EnrollmentRepositoryCustom {
    List<Enrollment> findAllByEvent(Long eventId);
}
