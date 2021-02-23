package com.studyolleh.modules.study.repository;

import com.querydsl.jpa.JPQLQuery;
import com.studyolleh.modules.account.domain.QAccount;
import com.studyolleh.modules.study.domain.QStudy;
import com.studyolleh.modules.study.domain.QStudyAccount;
import com.studyolleh.modules.study.domain.StudyAccount;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class StudyAccountRepositoryImpl extends QuerydslRepositorySupport implements StudyAccountRepositoryCustom{

    public StudyAccountRepositoryImpl() {
        super(StudyAccount.class);
    }

    @Override
    public List<StudyAccount> findMembersByStudyIdAndIsManager(Long studyId, boolean isManager) {
        QStudyAccount studyAccount = QStudyAccount.studyAccount;
        JPQLQuery<StudyAccount> query = from(studyAccount)
                .innerJoin(studyAccount.study, QStudy.study).fetchJoin()
                .innerJoin(studyAccount.account, QAccount.account).fetchJoin()
                .where(studyAccount.isManager.eq(isManager)
                        .and(QStudy.study.id.eq(studyId)));
        return query.fetch();
    }

}
