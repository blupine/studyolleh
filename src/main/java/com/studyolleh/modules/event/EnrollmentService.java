package com.studyolleh.modules.event;

import com.studyolleh.modules.account.Account;
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
