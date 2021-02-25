package com.studyolleh.modules.account.repository;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.domain.QAccount;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

public class AccountRepositoryImpl extends QuerydslRepositorySupport implements AccountRepositoryCustom{

    public AccountRepositoryImpl() {
        super(Account.class);
    }

    @Override
    public List<Account> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        JPQLQuery<Account> query = from(account)
                .distinct()
                .leftJoin(account.tags)
                .leftJoin(account.zones)
                .where(
                        account.tags.any().tag.in(tags),
                        account.zones.any().zone.in(zones));
        return query.fetch();
    }
}
