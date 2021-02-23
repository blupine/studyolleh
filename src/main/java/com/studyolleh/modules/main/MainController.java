package com.studyolleh.modules.main;

import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.modules.account.authentication.CurrentAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.event.domain.Enrollment;
import com.studyolleh.modules.event.service.EnrollmentService;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountService accountService;
    private final StudyService studyService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model){
        model.addAttribute("studyList", studyService.getRecent9StudyForIndexPage());

        if(account != null){


            Account loadedAccount = accountService.getAccountWithTagsAndZones(account);
            List<Enrollment> enrollmentList = enrollmentService.getEnrollmentByAccountWithEventAndStudy(account);
            List<Study> asManager = studyService.getRecent5StudyContainingAsManager(account);
            List<Study> asMember = studyService.getRecent5StudyContainingAsMember(account);
            List<Study> studyList = studyService.getStudyContainingTagsAndZones(accountService.getTags(loadedAccount), accountService.getZones(loadedAccount));
            model.addAttribute(loadedAccount);
            model.addAttribute("enrollmentList", enrollmentList);
            model.addAttribute("studyList", studyList);
            model.addAttribute("studyManagerOf", asManager);
            model.addAttribute("studyMemberOf", asMember);
            return "index-after-login";
        }

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String searchStudy(String keyword, Model model, @PageableDefault(
                                      size = 9,
                                      sort = "publishedDateTime",
                                      direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Study> studyPage = studyService.searchByKeyword(keyword, pageable);
        model.addAttribute("studyPage", studyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }
}
