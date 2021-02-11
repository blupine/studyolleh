package com.studyolleh.modules.account;

import com.querydsl.core.types.Predicate;
import com.studyolleh.modules.tag.Tag;
import com.studyolleh.modules.zone.Zone;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZoens(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }

}
