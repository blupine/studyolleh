package com.studyolleh.modules.study.domain;

import com.studyolleh.modules.zone.domain.Zone;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class StudyZoneItem {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    public static StudyZoneItem createZoneItem(Study study, Zone zone) {
        return StudyZoneItem.builder().study(study).zone(zone).build();
    }

}
