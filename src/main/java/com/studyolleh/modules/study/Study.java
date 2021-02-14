package com.studyolleh.modules.study;

import com.studyolleh.modules.account.Account;
import com.studyolleh.modules.account.UserAccount;
import com.studyolleh.modules.tag.Tag;
import com.studyolleh.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdateDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    private int memberCount;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public void addMember(Account account) {
        this.members.add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        this.members.remove(account);
        this.memberCount--;
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return isJoinable(account);
    }

    public boolean isJoinable(Account account) {
        return this.isPublished() && this.isRecruiting()
                && !this.managers.contains(account) && !this.members.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
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

    public boolean isManagedBy(Account account) {
        return this.managers.contains(account);
    }
}
