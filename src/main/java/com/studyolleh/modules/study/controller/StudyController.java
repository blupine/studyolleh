package com.studyolleh.modules.study.controller;

import com.studyolleh.modules.account.authentication.CurrentAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.study.domain.StudyAccount;
import com.studyolleh.modules.study.repository.StudyRepository;
import com.studyolleh.modules.study.service.StudyService;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.form.StudyForm;
import com.studyolleh.modules.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyRepository studyRepository;
    private final StudyFormValidator studyFormValidator;
    private final StudyService studyService;
    private final ModelMapper modelMapper;

    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentAccount Account account, @Valid StudyForm studyForm, Errors errors, Model model) {
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);
        return "redirect:/study/" + newStudy.getEncodedPath();
    }

    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentAccount Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudy(path);
        List<StudyAccount> studyMembers = studyService.getStudyMembersAndManagers(study);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("studyMembers", studyMembers);
        return "study/members";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersAndManagersByPath(path);
        studyService.addMember(study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }

    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersAndManagersByPath(path);
        studyService.removeMember(study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }
}
