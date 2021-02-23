package com.studyolleh.modules.study.repository;

import com.studyolleh.modules.study.domain.StudyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyAccountRepository extends JpaRepository<StudyAccount, Long>, StudyAccountRepositoryCustom{


}
