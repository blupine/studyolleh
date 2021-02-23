package com.studyolleh.modules.study.validator;

import com.studyolleh.modules.study.repository.StudyRepository;
import com.studyolleh.modules.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepositry;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(StudyForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;
        if(studyRepositry.existsByPath(studyForm.getPath())){
            errors.rejectValue("path", "wrong.path", "해당 스터디 경로값을 사용할 수 없습니다.");
        }
    }
}
