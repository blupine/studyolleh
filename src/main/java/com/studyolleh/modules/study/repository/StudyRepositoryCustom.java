package com.studyolleh.modules.study.repository;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.study.domain.Study;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryCustom {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    List<Study> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones);


}
