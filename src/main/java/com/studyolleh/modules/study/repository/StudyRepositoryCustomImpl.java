package com.studyolleh.modules.study.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;

import com.studyolleh.modules.study.domain.QStudy;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class StudyRepositoryCustomImpl extends QuerydslRepositorySupport implements StudyRepositoryCustom {

    public StudyRepositoryCustomImpl() {
        super(Study.class);
    }

    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().tag.title.containsIgnoreCase(keyword))
                .or(study.zones.any().zone.localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags).fetchJoin()
                .leftJoin(study.zones).fetchJoin()
                .distinct();

        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> results = pageableQuery.fetchResults();
        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    @Override
    public List<Study> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study)
                .where(study.published.isTrue()
                .and(study.closed.isFalse())
                .and(study.tags.any().tag.in(tags))
                .and(study.zones.any().zone.in(zones)))
                .leftJoin(study.tags).fetchJoin()
                .leftJoin(study.zones).fetchJoin()
                .orderBy(study.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}
