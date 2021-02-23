package com.studyolleh.modules.event.service;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.event.domain.Enrollment;
import com.studyolleh.modules.event.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public List<Enrollment> getEnrollmentByAccountWithEventAndStudy(Account account) {
        return enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(account, true);
    }

}
