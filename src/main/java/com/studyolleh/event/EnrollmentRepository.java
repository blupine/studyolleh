package com.studyolleh.event;

import com.studyolleh.domain.Account;
import com.studyolleh.domain.Enrollment;
import com.studyolleh.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);

}
