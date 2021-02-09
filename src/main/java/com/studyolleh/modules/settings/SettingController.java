package com.studyolleh.modules.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.modules.account.AccountService;
import com.studyolleh.modules.account.CurrentAccount;
import com.studyolleh.modules.domain.Account;
import com.studyolleh.modules.domain.Tag;
import com.studyolleh.modules.domain.Zone;
import com.studyolleh.modules.settings.form.*;
import com.studyolleh.modules.settings.validator.NicknameValidator;
import com.studyolleh.modules.settings.validator.PasswordFormValidator;
import com.studyolleh.modules.tag.TagForm;
import com.studyolleh.modules.tag.TagRepository;
import com.studyolleh.modules.tag.TagService;
import com.studyolleh.modules.zone.ZoneForm;
import com.studyolleh.modules.zone.ZoneRepository;
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
    public static final String ZONES_URL = "/zones";

    public static final String PROFILE_VIEW_NAME = SETTINGS + PROFILE_URL;
    public static final String PASSWORD_VIEW_NAME = SETTINGS + PASSWORD_URL;
    public static final String NOTIFICATION_VIEW_NAME = SETTINGS + NOTIFICATION_URL;
    public static final String ACCOUNT_VIEW_NAME = SETTINGS + ACCOUNT_URL;
    public static final String TAGS_VIEW_NAME = SETTINGS + TAGS_URL;
    public static final String ZONES_VIEW_NAME = SETTINGS + ZONES_URL;

    private final AccountService accountService;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
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
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS_URL + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag byTitle = tagRepository.findByTitle(title);
        if (byTitle == null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTag(account, byTitle);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES_URL)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones);

        List<String> zoneList = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(zoneList));
        return ZONES_VIEW_NAME;
    }

    @PostMapping(ZONES_URL + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES_URL + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }
}
