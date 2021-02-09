package com.studyolleh.modules.account;

import com.studyolleh.modules.domain.Account;
import com.studyolleh.infra.mail.EmailMessage;
import com.studyolleh.infra.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    private static final String testNickname = "testnick";
    private static final String testEmail = "skwint11@naver.com";
    private static final String testFailEmail = "asdfsadf";
    private static final String testPassword = "12341234";

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountRepository accountRepository;

    @MockBean
    private EmailService emailService;

    @DisplayName("회원 가입 화면 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get(AccountController.SIGNUP_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.SIGNUP_VIEW_NAME))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post(AccountController.SIGNUP_URL)
                .param("nickname", testNickname)
                .param("email", testFailEmail)
                .param("password", testPassword)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(AccountController.SIGNUP_VIEW_NAME));
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post(AccountController.SIGNUP_URL)
                .param("nickname", testNickname)
                .param("email", testEmail)
                .param("password", testPassword)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));


        Account account = accountRepository.findByEmail(testEmail);
        assertNotNull(account);
        assertNotEquals(account.getPassword(), testPassword);
        assertNotNull(account.getEmailCheckToken());
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken_with_correct_input() throws Exception{
        // signup
        Account account = Account.builder()
                .email(testEmail)
                .password(testPassword)
                .nickname(testNickname)
                .build();

        Account save = accountRepository.save(account);
        save.generateEmailCheckToken();
        mockMvc.perform(get(AccountController.CHECK_EMAIL_TOKEN_URL)
                .param("token", save.getEmailCheckToken())
                .param("email", save.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name(AccountController.CHECK_EMAIL_TOKEN_VIEW_NAME));
    }


    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception{
        mockMvc.perform(get(AccountController.CHECK_EMAIL_TOKEN_URL)
                .param("token", "failtoke")
                .param("email", "fail@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name(AccountController.CHECK_EMAIL_TOKEN_VIEW_NAME))
                .andExpect(unauthenticated());
    }

}