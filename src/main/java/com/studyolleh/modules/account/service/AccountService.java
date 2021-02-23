package com.studyolleh.modules.account.service;

import com.studyolleh.modules.account.authentication.UserAccount;
import com.studyolleh.modules.account.domain.Account;
import com.studyolleh.modules.account.domain.TagItem;
import com.studyolleh.modules.account.domain.ZoneItem;
import com.studyolleh.modules.account.form.SignUpForm;
import com.studyolleh.infra.config.AppProperties;
import com.studyolleh.modules.account.repository.AccountRepository;
import com.studyolleh.modules.account.repository.TagItemRepository;
import com.studyolleh.modules.account.repository.ZoneItemRepository;
import com.studyolleh.modules.tag.domain.Tag;
import com.studyolleh.modules.zone.domain.Zone;
import com.studyolleh.infra.mail.EmailMessage;
import com.studyolleh.infra.mail.EmailService;
import com.studyolleh.modules.account.form.Notifications;
import com.studyolleh.modules.account.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final TagItemRepository tagItemRepository;
    private final ZoneItemRepository zoneItemRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        context.setVariable("linkName", "이메일 인증하기");

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String eamilOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(eamilOrNickname);
        if(account == null){
            account = accountRepository.findByNickname(eamilOrNickname);
        }
        if(account == null){
            return null;
        }
        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    /**
     * @param account : 비영속 상태 account 객체인점 주의
     *                SettingsController.updateProfile(@CurrentUser account..) 에서 받아온 account라서 준영속 상태임
     *               따라서 accountRepository의 save 호출을 통해 merge해야 함
     * @param profile
     */
    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPasasword) {
        account.setPassword(passwordEncoder.encode(newPasasword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();

        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }


    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        Set<TagItem> tags = byId.orElseThrow(IllegalStateException::new).getTags();
        return tags.stream().map(TagItem::getTag).collect(Collectors.toSet());
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.addTagItem(tagItemRepository.save(TagItem.createTagItem(account, tag))));
    }

    public void removeTag(Account account, Tag tag){
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.removeTag(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        Set<ZoneItem> zones = byId.orElseThrow(IllegalStateException::new).getZones();
        return zones.stream().map(ZoneItem::getZone).collect(Collectors.toSet());
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.addZoneItem(zoneItemRepository.save(ZoneItem.createZoneItem(account, zone))));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.removeZone(zone));
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if (account == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다");
        }
        return account;
    }

    public Account getAccountWithTagsAndZones(Account account) {
        return accountRepository.findAccountWithZonesAndTagsById(account.getId());
    }
}
