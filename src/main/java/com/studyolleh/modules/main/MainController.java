package com.studyolleh.modules.main;

import com.studyolleh.modules.account.CurrentAccount;
import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.study.Study;
import com.studyolleh.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyService studyService;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model){
        if(account != null){
            model.addAttribute("account", account);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/search/study")
    public String searchStudy(String keyword, Model model) {
        List<Study> studyList = studyService.searchByKeyword(keyword);
        model.addAttribute(studyList);
        model.addAttribute("keyword", keyword);
        return "search";
    }
}
