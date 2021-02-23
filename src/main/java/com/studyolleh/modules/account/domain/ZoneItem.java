package com.studyolleh.modules.account.domain;

import com.studyolleh.modules.zone.domain.Zone;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class ZoneItem {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    public static ZoneItem createZoneItem(Account account, Zone zone) {
        return ZoneItem.builder().account(account).zone(zone).build();
    }

}
