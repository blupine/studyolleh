package com.studyolleh.modules.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolleh.modules.account.CurrentAccount;
import com.studyolleh.modules.domain.Account;
import com.studyolleh.modules.domain.Study;
import com.studyolleh.modules.domain.Tag;
import com.studyolleh.modules.domain.Zone;
import com.studyolleh.modules.study.form.StudyDescriptionForm;
import com.studyolleh.modules.tag.TagForm;
import com.studyolleh.modules.tag.TagRepository;
import com.studyolleh.modules.tag.TagService;
import com.studyolleh.modules.zone.ZoneForm;
import com.studyolleh.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingController {

    private final ModelMapper modelMapper;
    private final StudyService studyService;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/description")
    public String viewStudySetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentAccount Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm studyDescriptionForm,
                                  Errors errors, Model model, RedirectAttributes redirectAttributes) {

        Study study = studyService.getStudyToUpdate(account, path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        redirectAttributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/description";
    }

    @GetMapping("/banner")
    public String viewBannerSetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String updateBannerSetting(@CurrentAccount Account account, @PathVariable String path,
                                      String image, RedirectAttributes redirectAttributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        redirectAttributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableBannerSetting(@CurrentAccount Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + study.getEncodedPath() + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableBannerSetting(@CurrentAccount Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + study.getEncodedPath() + "/settings/banner";
    }

    @GetMapping("/tags")
    public String studyTagsForm(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));

        List<String> list = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute("whitelist", objectMapper.writeValueAsString(list));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdate(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String studyZonesForm(@CurrentAccount Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("zones", study.getZones().stream()
                .map(Zone::toString).collect(Collectors.toList()));

        List<String> list = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(list));
        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity remove(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvince());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studySettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }

    @PostMapping("study/publish")
    public String publishStudy(@CurrentAccount Account account, @PathVariable String path, Model model,
                               RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        model.addAttribute(account);
        model.addAttribute(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/"+ study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("study/close")
    public String closeStudy(@CurrentAccount Account account, @PathVariable String path, Model model,
                             RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        model.addAttribute(account);
        model.addAttribute(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path, Model model,
                               RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "인원 모집 설정은 한 시간에 한 번 변경 가능합니다.");
            return "redirect:/study/"+ study.getEncodedPath() + "/settings/study";
        }
        studyService.startRecruit(study);
        model.addAttribute(account);
        model.addAttribute(study);
        attributes.addFlashAttribute("message", "스터디 인원 모집을 시작했습니다.");
        return "redirect:/study/"+ study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path, Model model,
                              RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(!study.canUpdateRecruiting()){
            attributes.addFlashAttribute("message", "인원 모집 설정은 한 시간에 한 번 변경 가능합니다.");
            return "redirect:/study/"+ study.getEncodedPath() + "/settings/study";
        }
        studyService.stopRecruit(study);
        model.addAttribute(account);
        model.addAttribute(study);
        attributes.addFlashAttribute("message", "스터디 인원 모집을 종료했습니다.");
        return "redirect:/study/"+ study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("study/path")
    public String updatePath(@CurrentAccount Account account, @PathVariable String path, @RequestParam String newPath,
                             Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "해당 스터디 경로는 사용할 수 없습니다.");
            return "study/settings/study";
        }
        studyService.updateStudyPath(study, newPath);
        attributes.addFlashAttribute("message", "스터디 경로를 수정하였습니다.");
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("study/title")
    public String updateTitle(@CurrentAccount Account account, @PathVariable String path, @RequestParam String newTitle,
                              Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "해당 스터디 이름은 사용할 수 없습니다.");
            return "study/settings/study";
        }
        studyService.updateStudyTitle(study, newTitle);
        return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
    }

    @PostMapping("study/remove")
    public String removeStudy(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.removeStudy(study);
        return "redirect:/";
    }
}
