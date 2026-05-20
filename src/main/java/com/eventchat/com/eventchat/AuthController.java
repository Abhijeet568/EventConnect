package com.eventchat;

import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserProfileRepository userRepo;
    private final EmailService emailService;

    public AuthController(UserProfileRepository userRepo, EmailService emailService) {
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    // SHA-256 Password Hashing Utility
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) { throw new RuntimeException("Hashing failed"); }
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> payload) {
        String username = payload.get("username").trim();
        String email = payload.get("email").trim();
        String password = payload.get("password");

        if (userRepo.findById(username).isPresent()) return "USERNAME_TAKEN";
        if (userRepo.findByEmail(email).isPresent()) return "EMAIL_TAKEN";

        userRepo.save(new UserProfile(username, email, hashPassword(password)));
        return "SUCCESS";
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email").trim();
        String password = payload.get("password");

        Optional<UserProfile> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getPasswordHash().equals(hashPassword(password))) {
            return userOpt.get().getUsername(); // Return username on success
        }
        return "FAIL";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email").trim();
        Optional<UserProfile> userOpt = userRepo.findByEmail(email);

        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(999999));
            user.setOtpCode(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(10)); // Expires in 10 mins
            userRepo.save(user);

            emailService.sendOtpEmail(email, otp);
            return "OTP_SENT";
        }
        return "EMAIL_NOT_FOUND";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email").trim();
        String otp = payload.get("otp").trim();
        String newPassword = payload.get("newPassword");

        Optional<UserProfile> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            if (user.getOtpCode() != null && user.getOtpCode().equals(otp)) {
                if (LocalDateTime.now().isBefore(user.getOtpExpiry())) {
                    user.setPasswordHash(hashPassword(newPassword));
                    user.setOtpCode(null); // Clear OTP
                    user.setOtpExpiry(null);
                    userRepo.save(user);
                    return "SUCCESS";
                }
                return "OTP_EXPIRED";
            }
            return "INVALID_OTP";
        }
        return "FAIL";
    }
}