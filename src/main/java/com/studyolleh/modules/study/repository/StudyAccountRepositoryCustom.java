package com.studyolleh.modules.study.repository;

import com.studyolleh.modules.study.domain.StudyAccount;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyAccountRepositoryCustom {

    List<StudyAccount> findMembersByStudyIdAndIsManager(Long studyId, boolean isManager);

}
