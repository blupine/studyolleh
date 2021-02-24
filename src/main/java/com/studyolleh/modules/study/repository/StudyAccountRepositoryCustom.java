package com.studyolleh.modules.study.repository;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.domain.StudyAccount;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyAccountRepositoryCustom {

    List<StudyAccount> findMembersByStudyIdAndIsManager(Long studyId, boolean isManager);

    List<StudyAccount> findMembersAndManagersByStudyId(Long studyId);

    List<StudyAccount> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<StudyAccount> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    StudyAccount findStudyAccountWithAccountAndStudyByPathAndIsManager(String path, boolean isManager);

    StudyAccount findStudyAccountWithAccountAndStudyAndStudyTagsByPath(String path);

    StudyAccount findStudyAccountWithAccountAndStudyAndStudyZonesByPath(String path);
}
