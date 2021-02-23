package com.studyolleh.modules.event.repository;

import com.querydsl.jpa.JPQLQuery;
import com.studyolleh.modules.event.domain.Enrollment;
import com.studyolleh.modules.event.domain.QEnrollment;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class EnrollmentRepositoryImpl extends QuerydslRepositorySupport implements EnrollmentRepositoryCustom{

    public EnrollmentRepositoryImpl() {
        super(Enrollment.class);
    }

    @Override
    public List<Enrollment> findAllByEvent(Long eventId) {
        QEnrollment enrollment = QEnrollment.enrollment;
        JPQLQuery<Enrollment> query = from(enrollment).where(enrollment.event.id.eq(eventId))
                .leftJoin(enrollment.event).fetchJoin()
                .leftJoin(enrollment.account).fetchJoin()
                .distinct();
        return query.fetch();
    }
}
