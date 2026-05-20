package com.eventchat;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Event Connect Password Reset OTP");
        message.setText("Hello,\n\nYour OTP for resetting your password is: " + otp + "\n\nThis OTP will expire in 10 minutes.\n\nEvent Connect Team");
        mailSender.send(message);
    }
}