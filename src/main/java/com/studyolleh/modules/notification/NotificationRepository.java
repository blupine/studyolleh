package com.studyolleh.modules.notification;

import com.studyolleh.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Long countByAccountAndChecked(Account account, boolean checked);
}
