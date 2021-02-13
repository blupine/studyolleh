package com.studyolleh.modules.notification;

import com.studyolleh.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Long countByAccountAndChecked(Account account, boolean checked);

    List<Notification> findByAccountAndCheckedOrderByCreatedLocalDateTime(Account account, boolean checked);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean checked);

}
