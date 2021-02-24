package com.studyolleh.modules.study.domain;

import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.authentication.UserAccount;
import com.studyolleh.modules.account.domain.TagItem;
import com.studyolleh.modules.account.domain.ZoneItem;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    @Column(name = "study_id")
    private Long id;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private Set<StudyAccount> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudyTagItem> tags = new HashSet<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudyZoneItem> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdateDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    private int memberCount;


    public void addMember(StudyAccount account) {
        this.members.add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        boolean isRemoved = this.members.removeIf(studyAccount -> studyAccount.getAccount().getId().equals(account.getId()));
        this.memberCount -= isRemoved ? 1 : 0;
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return isJoinable(account);
    }

    public boolean isJoinable(Account account) {
        return this.isPublished() && this.isRecruiting()
                && !this.members.stream().map(studyAccount -> studyAccount.getAccount().getId()).collect(Collectors.toSet()).contains(account.getId());
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.stream().filter(sa -> !sa.isManager())
                .map(StudyAccount::getId).collect(Collectors.toSet()).contains(userAccount.getAccount().getId());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.members.stream().filter(sa -> sa.isManager())
                .map(StudyAccount::getId).collect(Collectors.toSet()).contains(userAccount.getAccount().getId());
    }

    public boolean isManagedBy(Account account) {
        return this.members.stream().filter(sa -> sa.isManager())
                .map(StudyAccount::getId).collect(Collectors.toSet()).contains(account.getId());
    }

    public boolean isMember(Account account) {
        return this.members.stream().filter(sa -> !sa.isManager())
                .map(StudyAccount::getId).collect(Collectors.toSet()).contains(account.getId());
    }

    public String getImage() {
        return this.image != null ? this.image : "/images/default_banner.png";
    }

    public void publish() {
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if (!this.closed && this.published) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("스터디를 종료할 수 없습니다. 이미 종료했거나 공개하지 않았습니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("인원 모집을 시작할 수 없습니다. 스터디를 공개하지 않았거나 한시간 뒤 다시 시도하세요.");
        }
    }

    public void stopRecruiting() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdateDateTime = LocalDateTime.now();
        }
        else{
            throw new IllegalArgumentException("인원 모집을 종료할 수 없습니다. 스터디를 공개하지 않았거나 한시간 뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdateDateTime == null || this.recruitingUpdateDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public boolean isRemovable() {
        return !this.published; // 모임을 했던 스터디는 삭제할 수 없음
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public void addTagItem(StudyTagItem tagItem) {
        this.tags.add(tagItem);
        tagItem.setStudy(this);
    }

    public void removeTag(Tag tag) {
        this.tags.removeIf(tagItem -> tagItem.getTag().equals(tag));
    }

    public void addZoneItem(StudyZoneItem zoneItem) {
        this.zones.add(zoneItem);
        zoneItem.setStudy(this);
    }

    public void removeZone(Zone zone) {
        this.zones.removeIf(zoneItem -> zoneItem.getZone().equals(zone));
    }
}
