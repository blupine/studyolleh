package com.studyolleh.modules.study;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyFactory {

    @Autowired
    StudyService studyService;

    public Study createStudy(String path, Account account) {
        Study study = new Study();
        study.setPath(path);

        studyService.createNewStudy(study, account);
        return study;
    }
}
