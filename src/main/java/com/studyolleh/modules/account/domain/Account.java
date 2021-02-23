package com.studyolleh.modules.account.domain;

import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id @GeneratedValue
    @Column(name = "account_id")
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime emailCheckTokenGeneratedAt;

    private LocalDateTime joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail; // 스터디 생성을 이메일로 알람을 받음

    private boolean studyCreatedByWeb = true;   // 스터디 생성을 웹으로 알람을 받음

    private boolean studyEnrollmentResultByEmail; // 스터디 가입 신청 결과를 이메일로 받음

    private boolean studyEnrollmentResultByWeb = true;   // 스터디 가입 신청 결과를 웹으로 받

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TagItem> tags = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ZoneItem> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token){
        return this.getEmailCheckToken().equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void addTagItem(TagItem tagItem) {
        this.tags.add(tagItem);
        tagItem.setAccount(this);
    }

    public void removeTag(Tag tag) {
        this.tags.removeIf(tagItem -> tagItem.getTag().equals(tag));
    }

    public void addZoneItem(ZoneItem zoneItem) {
        this.zones.add(zoneItem);
        zoneItem.setAccount(this);
    }

    public void removeZone(Zone zone) {
        this.zones.removeIf(zoneItem -> zoneItem.getZone().equals(zone));
    }
}
