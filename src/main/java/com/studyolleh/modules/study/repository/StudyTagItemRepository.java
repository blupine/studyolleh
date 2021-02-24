package com.studyolleh.modules.study.repository;

import com.studyolleh.modules.study.domain.StudyTagItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyTagItemRepository extends JpaRepository<StudyTagItem, Long> {
}
