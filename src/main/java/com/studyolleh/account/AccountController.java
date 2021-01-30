package com.studyolleh.account;

import com.studyolleh.account.form.SignUpForm;
import com.studyolleh.account.validator.SignUpFormValidator;
import com.studyolleh.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    public static final String SIGNUP_VIEW_NAME = "account/signup";
    public static final String SIGNUP_URL = "/signup";
    public static final String CHECK_EMAIL_TOKEN_VIEW_NAME = "account/checked-email";
    public static final String CHECK_EMAIL_TOKEN_URL = "/check-email-token";
    public static final String CHECK_EMAIL_VIEW_NAME = "account/check-email";
    public static final String CHECK_EMAIL_URL = "/check-email";
    public static final String RESEND_CONFIRM_EMAIL_URL = "/resend-confirm-email";
    public static final String PROFILE_URL = "/profile";
    public static final String PROFILE_VIEW_NAME = "account/profile";
    public static final String EMAIL_LOGIN_URL = "/email-login";
    public static final String EMAIL_LOGIN_VIEW_NAME = "account/email-login";
    public static final String LOGIN_BY_EMAIL_URL = "login-by-email";
    public static final String LOGIN_BY_EMAIL_VIEW_NAME = "account/logged-in-by-email";

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);   // 입력 폼 검증, nickname, email 중복 확인
    }

    @GetMapping(SIGNUP_URL)
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return SIGNUP_VIEW_NAME;
    }

    @PostMapping(SIGNUP_URL)
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
        if (errors.hasErrors()) {
            return SIGNUP_VIEW_NAME;
        }
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping(CHECK_EMAIL_TOKEN_URL)
    public String checkEmailToken(String token, String email, Model model){
        Account account = accountRepository.findByEmail(email);
        if(account == null){
            model.addAttribute("error", "wrong.email");
            return CHECK_EMAIL_TOKEN_VIEW_NAME;
        }

        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return CHECK_EMAIL_TOKEN_VIEW_NAME;
        }

        accountService.completeSignUp(account);

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return CHECK_EMAIL_TOKEN_VIEW_NAME;
    }

    @GetMapping(CHECK_EMAIL_URL)
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return CHECK_EMAIL_VIEW_NAME;
    }


    @GetMapping(RESEND_CONFIRM_EMAIL_URL)
    public String resendConfirmMail(@CurrentUser Account account, Model model) {
        if(!account.canSendConfirmEmail()){
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 재전송 가능합니다.");
            model.addAttribute("email", account.getEmail());
            return CHECK_EMAIL_VIEW_NAME;
        }
        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping(PROFILE_URL + "/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        if(nickname == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        Account byNickname = accountRepository.findByNickname(nickname);
        model.addAttribute(byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));
        return PROFILE_VIEW_NAME;
    }

    @GetMapping(EMAIL_LOGIN_URL)
    public String emailLoginForm(){
        return "account/email-login";
    }

    @PostMapping(EMAIL_LOGIN_URL)
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes redirectAttributes) {
        Account byEmail = accountRepository.findByEmail(email);
        if(byEmail == null){
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return EMAIL_LOGIN_VIEW_NAME;
        }

        if(!byEmail.canSendConfirmEmail()){
            model.addAttribute("error", "이메일 로그인은 1시간에 한 번 발송 가능합니다.");
            return EMAIL_LOGIN_VIEW_NAME;
        }

        accountService.sendLoginLink(byEmail);
        redirectAttributes.addFlashAttribute("message", "로그인 링크를 이메일로 보내드렸습니다.");
        return "redirect:" + EMAIL_LOGIN_URL;
    }

    @GetMapping(LOGIN_BY_EMAIL_URL)
    public String loginByEmail(String token, String email, Model model) {
        Account byEmail = accountRepository.findByEmail(email);
        if(byEmail == null || byEmail.isValidToken(token)){
            model.addAttribute("error", "로그인할 수 없습니다.");
            return LOGIN_BY_EMAIL_VIEW_NAME;
        }
        accountService.login(byEmail);
        return LOGIN_BY_EMAIL_VIEW_NAME;
    }

}
