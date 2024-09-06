package com.tutor.securityservice.service;

import com.tutor.securityservice.dto.*;
import com.tutor.securityservice.repo.UserRepo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public ResponseEntity<String> signup(UserRegisterDto input) {
        if (input.getEmail() == null || input.getEmail().isEmpty()) {
           return ResponseEntity.badRequest().body("Email is required");
        }
        if (userRepository.findByUsername(input.getUsername()).isPresent()) {
            log.info("Username already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists");
        }
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            log.info("Email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        }


        // Validate email format
        if (!isValidEmail(input.getEmail())) {
            log.info("Invalid email format");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
        }

        if (!isValidPassword(input.getPassword())) {
            log.info("Weak password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Weak password");
        }



        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
         userRepository.save(user);
         return ResponseEntity.status(HttpStatus.CREATED).body("Otp sent successfully");
    }
    public User authenticate(UserLoginDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return user;
    }
    public ResponseEntity<String> forgetPassword(ForgotPassDto email) {
      if (email.getEmail() == null || email.getEmail().isEmpty()) {
          return ResponseEntity.badRequest().body("Email is required");
      }
      if (!userRepository.findByEmail(email.getEmail()).isPresent()) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email not found");
      }
      Optional<User> optionalUser = userRepository.findByEmail(email.getEmail());
      User user = optionalUser.orElseThrow(() -> new RuntimeException("User not found"));

      user.setVerificationCode(generateVerificationCode());
      user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
      user.setEnabled(true);
      sendVerificationEmail(user);
      userRepository.save(user);
      return ResponseEntity.status(HttpStatus.OK).body("Otp sent successfully");



    }
    public ResponseEntity<String> resetPassword(ResetPassDto input){
        String email = input.getEmail();
        String verificationCode = input.getVerificationCode();
        String newPassword = input.getPassword();

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user = optionalUser.orElseThrow(() -> new RuntimeException("User not found"));

        if (!userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with this email.");
        }

        // Verify the code
        if (!user.getVerificationCode().equals(verificationCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid verification code.");
        }
        if (!isValidPassword(input.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Weak password");
        }

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));  // Ensure to hash the password
        user.setVerificationCode(null);  // Clear the verification code after reset
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully.");
    }

    public void verifyUser(UserVerifyDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    private void sendVerificationEmail(User user) {
        if(
                user.getEmail() == null
        ){
            throw new RuntimeException("Email is null");
        }
        //TODO: Update with company logo
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }

    // Example of a simple email validation method
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    // Example of a simple password validation method
    private boolean isValidPassword(String password) {
        // Implement your own password strength validation logic
        // For example, check for minimum length, inclusion of digits, special characters, etc.
        return password.length() >= 8 &&
                password.matches(".*\\d.*") &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[@#$%^&+=].*");
    }


    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

}
