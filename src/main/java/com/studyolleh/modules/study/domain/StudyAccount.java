package com.studyolleh.modules.study.domain;

import com.studyolleh.modules.account.domain.Account;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class StudyAccount {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    private boolean isManager;

    public static StudyAccount createStudyAccount(Study study, Account account, boolean isManager) {
        return StudyAccount.builder()
                .account(account)
                .study(study)
                .isManager(isManager).build();
    }

}
