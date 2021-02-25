package com.studyolleh.modules.account.domain;

import com.studyolleh.modules.tag.domain.Tag;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class TagItem {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public static TagItem createTagItem(Account account, Tag tag) {
        return TagItem.builder().account(account).tag(tag).build();
    }

}
