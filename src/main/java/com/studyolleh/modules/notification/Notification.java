package com.studyolleh.modules.notification;

import com.studyolleh.modules.account.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @EqualsAndHashCode(of = "id")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id @GeneratedValue
    private Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne
    Account account;

    private LocalDateTime createdLocalDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

}

