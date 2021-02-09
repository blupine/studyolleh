package com.studyolleh.modules.event;

import com.studyolleh.modules.domain.Account;
import com.studyolleh.modules.domain.Enrollment;
import com.studyolleh.modules.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);

}
