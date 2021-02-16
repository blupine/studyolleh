package com.studyolleh.restapi.main;

import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.account.AccountService;
import com.studyolleh.modules.account.CurrentAccount;
import com.studyolleh.modules.event.Enrollment;
import com.studyolleh.modules.event.EnrollmentService;
import com.studyolleh.modules.study.Study;
import com.studyolleh.modules.study.StudyService;
import com.studyolleh.restapi.LoginService;
import com.studyolleh.restapi.common.Response;
import com.studyolleh.restapi.dto.AccountDto;
import com.studyolleh.restapi.dto.EnrollmentDto;
import com.studyolleh.restapi.dto.LoginRequestDto;
import com.studyolleh.restapi.dto.StudyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class MainApiController {

    private final AccountService accountService;
    private final StudyService studyService;
    private final EnrollmentService enrollmentService;
    private final ModelMapper modelMapper;
    private final LoginService loginService;

    @GetMapping("/")
    public ResponseEntity home(@CurrentAccount Account account) {

        if (account != null) {
            Account loadedAccount = accountService.getAccountWithTagsAndZones(account);
            List<Enrollment> enrollmentList = enrollmentService.getEnrollmentByAccountWithEventAndStudy(account);
            List<Study> asManager = studyService.getRecent5StudyContainingAsManager(account);
            List<Study> asMember = studyService.getRecent5StudyContainingAsMember(account);
            List<Study> studyList = studyService.getStudyContainingTagsAndZones(loadedAccount.getTags(), loadedAccount.getZones());
            HomeDto homeDto = convertDto(loadedAccount, enrollmentList, asManager, asMember, studyList);
            return new ResponseEntity<>(homeDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDto loginRequestDto) {
        Optional<AccountDto> optional = loginService.login(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        if (optional.isPresent()) {
            String authToken = loginService.createAuthToken(optional.get());
            return new ResponseEntity<>(new Response<>(authToken), HttpStatus.OK);
        }
        return new ResponseEntity<>(new Response<>(null), HttpStatus.OK);
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
