package com.studyolleh.modules.study;

import com.studyolleh.modules.tag.Tag;
import com.studyolleh.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    List<Study> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones);
}
