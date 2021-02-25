package com.studyolleh.modules.study.domain;

import com.studyolleh.modules.tag.domain.Tag;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class StudyTagItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public static StudyTagItem createTagItem(Study study, Tag tag) {
        return StudyTagItem.builder().study(study).tag(tag).build();
    }

}
