package com.example.Book.Store;

import com.twilio.Twilio;
import com.twilio.Twilio.*;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;

import java.util.Random;

@Configuration
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Async
    public  void sendEmail(String to, String subject, String body) {
        try {
            long startTime = System.currentTimeMillis();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("your-email@gmail.com");

            mailSender.send(message);

            long endTime = System.currentTimeMillis();
            System.out.println("Email sent in " + (endTime - startTime) + " ms");
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
    public int otp(){
        Random rand = new Random();
        return rand.nextInt(10000);
    }
    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;
    public void sendOtpSms(String toPhoneNumber, String otp) {
        Twilio.init(accountSid, authToken);

        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhoneNumber), // To
                new com.twilio.type.PhoneNumber(twilioPhoneNumber), // From (Twilio number)
                "Your OTP code is: " + otp
        ).create();

        System.out.println("OTP sent successfully! Message SID: " + message.getSid());
    }

}
