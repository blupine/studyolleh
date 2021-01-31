package com.studyolleh.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.account.AccountService;
import com.studyolleh.account.CurrentAccount;
import com.studyolleh.domain.Account;
import com.studyolleh.domain.Tag;
import com.studyolleh.settings.form.*;
import com.studyolleh.settings.validator.NicknameValidator;
import com.studyolleh.settings.validator.PasswordFormValidator;
import com.studyolleh.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    public static final String ROOT = "/";
    public static final String SETTINGS = "settings";
    public static final String PROFILE_URL = "/profile";
    public static final String PASSWORD_URL = "/password";
    public static final String NOTIFICATION_URL = "/notifications";
    public static final String ACCOUNT_URL = "/account";
    public static final String TAGS_URL = "/tags";

    public static final String PROFILE_VIEW_NAME = SETTINGS + "/profile";
    public static final String PASSWORD_VIEW_NAME = SETTINGS + "/password";
    public static final String NOTIFICATION_VIEW_NAME = SETTINGS + "/notifications";
    public static final String ACCOUNT_VIEW_NAME = SETTINGS + "/account";
    public static final String TAGS_VIEW_NAME = SETTINGS + "/tags";

    private final AccountService accountService;
    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final PasswordFormValidator passwordFormValidator;
    private final NicknameValidator nicknameFormValidator;
    private final ObjectMapper objectMapper;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(passwordFormValidator);
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }


    @GetMapping(PROFILE_URL)
    public String profileUpdateForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return PROFILE_VIEW_NAME;
    }

    @PostMapping(PROFILE_URL)
    public String updateProfile(@CurrentAccount Account account, @Valid Profile profile,
                                Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return PROFILE_VIEW_NAME;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/" + SETTINGS + PROFILE_URL;
    }

    @GetMapping(PASSWORD_URL)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return PASSWORD_VIEW_NAME;
    }

    @PostMapping(PASSWORD_URL)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm,
                                 Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return PASSWORD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        redirectAttributes.addFlashAttribute("message", "패스워드를 변경햇습니다.");
        return "redirect:/" + SETTINGS + PASSWORD_URL;
    }

    @GetMapping(NOTIFICATION_URL)
    public String updateNotificationForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return NOTIFICATION_VIEW_NAME;
    }

    @PostMapping(NOTIFICATION_URL)
    public String updateNotifications(@CurrentAccount Account account, @Valid Notifications notifications,
                                      Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return NOTIFICATION_VIEW_NAME;
        }

        accountService.updateNotifications(account, notifications);
        redirectAttributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/" + SETTINGS + NOTIFICATION_URL;
    }

    @GetMapping(ACCOUNT_URL)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return ACCOUNT_VIEW_NAME;
    }

    @PostMapping(ACCOUNT_URL)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm,
                                Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        redirectAttributes.addFlashAttribute("message", "닉네임 수정을 완료했습니다.");
        return "redirect:/" + SETTINGS + ACCOUNT_URL;
    }

    @GetMapping(TAGS_URL)
    public String updateTagsForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
        return TAGS_VIEW_NAME;
    }

    @PostMapping(TAGS_URL + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(title).build());
        }
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS_URL + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag byTitle = tagRepository.findByTitle(title);
        if(byTitle == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTags(account, byTitle);
        return ResponseEntity.ok().build();
    }
}
