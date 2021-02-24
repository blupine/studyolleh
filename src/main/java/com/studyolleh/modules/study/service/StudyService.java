package com.studyolleh.modules.study.service;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.study.domain.*;
import com.studyolleh.modules.study.event.StudyCreatedEvent;
import com.studyolleh.modules.study.event.StudyUpdateEvent;
import com.studyolleh.modules.study.repository.StudyAccountRepository;
import com.studyolleh.modules.study.repository.StudyRepository;
import com.studyolleh.modules.study.repository.StudyTagItemRepository;
import com.studyolleh.modules.study.repository.StudyZoneItemRepository;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import com.studyolleh.modules.study.form.StudyDescriptionForm;
import com.studyolleh.modules.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final ModelMapper modelMapper;
    private final StudyRepository studyRepository;
    private final StudyTagItemRepository studyTagItemRepository;
    private final StudyZoneItemRepository studyZoneItemRepository;
    private final StudyAccountRepository studyAccountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addMember(studyAccountRepository.save(StudyAccount.createStudyAccount(study, account, true)));
        return newStudy;
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path);
        checkIfManager(account, study);
        return study;
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm descriptionForm) {
        modelMapper.map(descriptionForm, study);
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디 소개를 수정했습니다."));
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public void addStudyTag(Study study, Tag tag) {
        study.addTagItem(
                studyTagItemRepository.save(StudyTagItem.createTagItem(study, tag))
        );
    }

    public void removeStudyTag(Study study, Tag tag) {
        study.removeTag(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.addZoneItem(
                studyZoneItemRepository.save(StudyZoneItem.createZoneItem(study, zone))
        );
    }

    public void removeZone(Study study, Zone zone) {
        study.removeZone(zone);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public void publish(Study study) {
        study.publish();
        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    public void close(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디를 종료했습니다."));
    }

    public void startRecruit(Study study) {
        study.startRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디 팀원 모집을 시작합니다"));
    }

    public void stopRecruit(Study study) {
        study.stopRecruiting();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디 팀원 모집을 종료합니다."));
    }

    private void checkIfManager(Account account, Study study) {
        if (!study.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public boolean isValidPath(String path) {
        if (!path.matches(StudyForm.VALID_PATH_PATTERN)) {
            return false;
        }
        return !studyRepository.existsByPath(path);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= StudyForm.VALID_TITLE_LENGTH;
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    public void removeStudy(Study study) {
        if (study.isRemovable()) {
            studyRepository.delete(study);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Study study, Account account) {
        if (study.isJoinable(account)) {
            study.addMember(studyAccountRepository.save(StudyAccount.createStudyAccount(study, account, false)));
        } else {
            throw new AccessDeniedException("스터디에 가입할 수 없습니다.");
        }
    }

    public void addManager(Study study, Account account) {
        if (study.isJoinable(account)) {
            study.addMember(studyAccountRepository.save(StudyAccount.createStudyAccount(study, account, true)));
        } else {
            throw new AccessDeniedException("스터디에 가입할 수 없습니다.");
        }
    }

    public void removeMember(Study study, Account account) {
        study.removeMember(account);
    }

    public List<Account> getStudyMembers(Study study) {
        List<StudyAccount> members = studyAccountRepository.findMembersByStudyIdAndIsManager(study.getId(), false);
        return members.stream().map(StudyAccount::getAccount).collect(Collectors.toList());
    }

    public List<Account> getStudyManagers(Study study) {
        List<StudyAccount> members = studyAccountRepository.findMembersByStudyIdAndIsManager(study.getId(), true);
        return members.stream().map(StudyAccount::getAccount).collect(Collectors.toList());
    }

    public List<StudyAccount> getStudyMembersAndManagers(Study study) {
        return studyAccountRepository.findMembersAndManagersByStudyId(study.getId());
    }

    public Study findStudyWithManagersByPath(String path) {
        return studyRepository.findStudyWithManagersByPath(path);
    }

    public Study findStudyToEnroll(String path) {
        Study study = studyRepository.findStudyOnlyByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public Page<Study> searchByKeyword(String keyword, Pageable pageable) {
         return studyRepository.findByKeyword(keyword, pageable);
    }

    public List<Study> getRecent9StudyForIndexPage() {
        return studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false);
    }

    public List<Study> getRecent5StudyContainingAsManager(Account account) {
        List<StudyAccount> studyAccounts = studyAccountRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
        return studyAccounts.stream().map(StudyAccount::getStudy).collect(Collectors.toList());
    }

    public List<Study> getRecent5StudyContainingAsMember(Account account) {
        List<StudyAccount> studyAccounts = studyAccountRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
        return studyAccounts.stream().map(StudyAccount::getStudy).collect(Collectors.toList());
    }

    public List<Study> getStudyContainingTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        return studyRepository.findByTagsAndZones(tags, zones);
    }

    public Set<Tag> getStudyTags(Study study) {
        return study.getTags().stream().map(StudyTagItem::getTag).collect(Collectors.toSet());
    }

    public Set<Zone> getStudyZones(Study study) {
        return study.getZones().stream().map(StudyZoneItem::getZone).collect(Collectors.toSet());
    }
}

