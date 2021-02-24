package com.studyolleh.modules.study.repository;

import com.querydsl.jpa.JPQLQuery;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.domain.QAccount;
import com.studyolleh.modules.account.domain.QTagItem;
import com.studyolleh.modules.study.domain.*;
import com.studyolleh.modules.tag.domain.QTag;
import com.studyolleh.modules.zone.domain.QZone;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

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

    @Override
    public List<StudyAccount> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed) {
        QStudyAccount studyAccount = QStudyAccount.studyAccount;
        JPQLQuery<StudyAccount> query = from(studyAccount)
                .innerJoin(studyAccount.study, QStudy.study).fetchJoin()
                .innerJoin(studyAccount.account, QAccount.account).fetchJoin()
                .where(
                        studyAccount.isManager.eq(true)
                                .and(QAccount.account.id.eq(account.getId())
                                        .and(QStudy.study.closed.eq(closed))));
        return query.fetch();
    }

    @Override
    public List<StudyAccount> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed) {
        QStudyAccount studyAccount = QStudyAccount.studyAccount;
        JPQLQuery<StudyAccount> query = from(studyAccount)
                .innerJoin(studyAccount.study, QStudy.study).fetchJoin()
                .innerJoin(studyAccount.account, QAccount.account).fetchJoin()
                .where(
                        studyAccount.isManager.eq(false)
                                .and(QAccount.account.id.eq(account.getId())
                                        .and(QStudy.study.closed.eq(closed))));
        return query.fetch();
    }

    @Override
    public List<StudyAccount> findMembersAndManagersByStudyId(Long studyId) {
        QStudyAccount studyAccount = QStudyAccount.studyAccount;
        JPQLQuery<StudyAccount> query = from(studyAccount)
                .innerJoin(studyAccount.study, QStudy.study).fetchJoin()
                .innerJoin(studyAccount.account, QAccount.account).fetchJoin()
                .where(QStudy.study.id.eq(studyId));
        return query.fetch();
    }

    @Override
    public StudyAccount findStudyAccountWithAccountAndStudyByPathAndIsManager(String path, boolean isManager) {
        QStudyAccount studyAccount = QStudyAccount.studyAccount;
        JPQLQuery<StudyAccount> query = from(studyAccount)
                .innerJoin(studyAccount.study, QStudy.study).fetchJoin()
                .innerJoin(studyAccount.account, QAccount.account).fetchJoin()
                .where(studyAccount.isManager.eq(isManager).and(QStudy.study.path.eq(path)));
        return query.fetchOne();
    }

    @Override
    public StudyAccount findStudyAccountWithAccountAndStudyAndStudyTagsByPath(String path) {
        QStudyAccount studyAccount = QStudyAccount.studyAccount;
        JPQLQuery<StudyAccount> query = from(studyAccount)
                .innerJoin(studyAccount.study, QStudy.study).fetchJoin()
                .innerJoin(studyAccount.account, QAccount.account).fetchJoin()
                .leftJoin(QStudy.study.tags, QStudyTagItem.studyTagItem).fetchJoin()
                .leftJoin(QStudyTagItem.studyTagItem.tag, QTag.tag).fetchJoin()
                .where(QStudy.study.path.eq(path));
        return query.fetchOne();
    }

    @Override
    public StudyAccount findStudyAccountWithAccountAndStudyAndStudyZonesByPath(String path) {
        QStudyAccount studyAccount = QStudyAccount.studyAccount;
        JPQLQuery<StudyAccount> query = from(studyAccount)
                .innerJoin(studyAccount.study, QStudy.study).fetchJoin()
                .innerJoin(studyAccount.account, QAccount.account).fetchJoin()
                .leftJoin(QStudy.study.zones, QStudyZoneItem.studyZoneItem).fetchJoin()
                .leftJoin(QStudyZoneItem.studyZoneItem.zone, QZone.zone).fetchJoin()
                .where(QStudy.study.path.eq(path));
        return query.fetchOne();
    }
}

