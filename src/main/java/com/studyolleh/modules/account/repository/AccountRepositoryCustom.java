package com.studyolleh.modules.account.repository;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface AccountRepositoryCustom {

    List<Account> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones);
}
