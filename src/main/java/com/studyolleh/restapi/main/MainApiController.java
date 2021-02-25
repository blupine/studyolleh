package com.studyolleh.restapi.main;

import com.studyolleh.modules.account.authentication.CurrentAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.service.AccountService;
import com.studyolleh.modules.event.domain.Enrollment;
import com.studyolleh.modules.event.service.EnrollmentService;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.study.service.StudyService;
import com.studyolleh.restapi.account.RestAccountService;
import com.studyolleh.restapi.dto.EnrollmentDto;
import com.studyolleh.restapi.dto.StudyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/", produces = MediaTypes.HAL_JSON_VALUE)
public class MainApiController {

    private final AccountService accountService;
    private final StudyService studyService;
    private final EnrollmentService enrollmentService;
    private final ModelMapper modelMapper;
    private final RestAccountService loginService;

    @GetMapping("/home")
    public ResponseEntity home(@CurrentAccount Account account){
        if (account != null) {
            Account loadedAccount = accountService.getAccountWithTagsAndZones(account);
            List<Enrollment> enrollmentList = enrollmentService.getEnrollmentByAccountWithEventAndStudy(account);
            List<Study> asManager = studyService.getRecent5StudyContainingAsManager(account);
            List<Study> asMember = studyService.getRecent5StudyContainingAsMember(account);
            List<Study> studyList = studyService.getStudyContainingTagsAndZones(accountService.getTags(loadedAccount), accountService.getZones(loadedAccount));

            HomeDto homeDto = convertDto(loadedAccount, enrollmentList, asManager, asMember, studyList);

            WebMvcLinkBuilder builder = WebMvcLinkBuilder.linkTo(MainApiController.class);
            URI uri = builder.toUri();

            EntityModel<HomeDto> entityModel = EntityModel.of(homeDto);
            entityModel.add(WebMvcLinkBuilder.linkTo(MainApiController.class).withSelfRel());
            entityModel.add(WebMvcLinkBuilder.linkTo(MainApiController.class).slash(account.getNickname()).withRel("detail"));
            return ResponseEntity.created(uri).body(entityModel);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }


    private HomeDto convertDto(Account account, List<Enrollment> enrollmentList, List<Study> asManager,
                               List<Study> asMember, List<Study> studyList) {
        HomeDto homeDto = HomeDto.builder()
                .nickname(account.getNickname())
                .email(account.getEmail())
                .profileImage(account.getProfileImage())
                .enrollmentList(enrollmentList.stream().map(e -> modelMapper.map(e, EnrollmentDto.class)).collect(Collectors.toList()))
                .asManager(asManager.stream().map(m -> modelMapper.map(m, StudyDto.class)).collect(Collectors.toList()))
                .asMember(asMember.stream().map(m -> modelMapper.map(m, StudyDto.class)).collect(Collectors.toList()))
                .studyList(studyList.stream().map(m -> modelMapper.map(m, StudyDto.class)).collect(Collectors.toList()))
                .build();

        return homeDto;
    }
    @Data
    @Builder
    @AllArgsConstructor
    static class HomeDto {
        private String nickname;
        private String email;
        private String profileImage;
        private List<EnrollmentDto> enrollmentList;
        private List<StudyDto> asManager;
        private List<StudyDto> asMember;
        private List<StudyDto> studyList;
    }


}
