package com.studyolleh.study;

import com.studyolleh.domain.Account;
import com.studyolleh.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepositry;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepositry.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

}
