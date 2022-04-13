package com.henrique.virtualteacher.utils;

import com.henrique.virtualteacher.entities.User;
import com.henrique.virtualteacher.entities.VerificationToken;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;

@Service
@AllArgsConstructor
public class MailSender {

    @Value("${spring.mail.username}")
    private static String SERVER_MAIL;

    @Value("${server.port}")
    private static String SERVER_PORT;

    private final static String REGISTRATION_MAIL_SUBJECT = "Virtual-Teacher Account Verification";
    private final static String SUPPORT_TEAM_MAIL = "virtual-teacher.help@gmail.com";
    private final static String REGISTRATION_VERIFICATION_URL = "/api/auth/verify";

    private final JavaMailSender mailSender;

    @Autowired
    public MailSender(JavaMailSenderImpl javaMailSender) {
        this.mailSender = javaMailSender;
    }

public static SimpleMailMessage createRegistrationMail(User mailReceiver, VerificationToken token, HttpServletRequest request) {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setFrom(SERVER_MAIL);
    mailMessage.setTo(mailReceiver.getEmail());
    mailMessage.setSentDate(Date.from(Instant.now()));
    mailMessage.setSubject(REGISTRATION_MAIL_SUBJECT);
    mailMessage.setText(createText(mailReceiver, token, request));
    return mailMessage;
}

public  void sendMail(SimpleMailMessage mailMessage) {
    mailSender.send(mailMessage);
}

private static String getUrl(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + REGISTRATION_VERIFICATION_URL;
}

private static String createText(User recipient, VerificationToken token, HttpServletRequest request) {
     String verificationLink = "To confirm your Transaction, please click the following link : "
             + getUrl(request) + "?token=" + token.getToken();

     return String.format("Hello %s,\n" +
             "\n" +
            "Thank you for joining Virtual Teacher.\n" +
            "\n" +
            "We’d like to confirm that your account was created successfully. To access your Virtual Teacher account, Click on the below provided link" +
            "so your account can be verified.\n" +
            "\n" +
            "%s\n" +
            "\n" +
            "If you experience any issues logging into your account, reach out to us at %s\n" +
            "\n" +
            "Best regards,\n" +
            "The Virtual Teacher team", recipient.getFirstName(),verificationLink, SUPPORT_TEAM_MAIL);
}

}
