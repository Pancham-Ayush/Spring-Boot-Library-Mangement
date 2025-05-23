package com.example.Book.Store.Controller;

import com.example.Book.Store.EmailService;
import com.example.Book.Store.Model.UserDetails;
import com.example.Book.Store.REPO.ApprovalRepo;
import com.example.Book.Store.Services.ApprovalService;
import com.example.Book.Store.Services.UserService;
import com.example.Book.Store.UserLoginConfig.CustomUserDetailsService;
import com.example.Book.Store.Model.UserApproval;
import com.example.Book.Store.UserLoginConfig.SecurityConfig;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
 class AuthController {

@Autowired
SecurityConfig securityConfig;
@Autowired
UserService userService;
@Autowired
CustomUserDetailsService customUserDetailsService;
@Autowired
ApprovalRepo approvalRepo;
@Autowired
ApprovalService approvalService;
@Autowired
EmailService emailService;

    @GetMapping("/login")
    public String loginPage() {

    return "login";
    }
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserApproval());
        return "register";
    }

    @PostMapping("/approval")
    public String submitUserApprovalRequest(@ModelAttribute UserApproval user, Model model) {

        if(userService.contains(user.getPhone())||approvalService.contains(user.getPhone())){
            model.addAttribute("useralreadyexixt", user);
            return "useralreadyexixt";
        }

        else {
            // Process the user's approval request
            approvalService.approveUser(user);

            // Compose a detailed email notification
            String emailSubject = "Account Verification - Pending Approval";
            String emailBody = String.format(
                    "Dear %s,\n\n"
                            + "Thank you for registering with us. Your request is currently under review by our verification team. "
                            + "We will carefully assess your details to ensure compliance with our security standards.\n\n"
                            + "Verification Details:\n"
                            + "- Name: %s\n"
                            + "- Email: %s\n"
                            + "- Contact Number: %s\n\n"
                            + "Our team aims to complete the verification process within the next 24-48 hours. "
                            + "Once your account is approved, you will receive a confirmation email with further instructions.\n\n"
                            + "If you have any questions, feel free to reach out to our support team.\n\n"
                            + "Best regards,\n"
                            + "The Support Team",
                    user.getName(), user.getName(), user.getEmail(), user.getPhone()
            );

            // Send the email
            emailService.sendEmail(user.getEmail(), emailSubject, emailBody);
          try {
              emailService.sendOtpSms("+91"+user.getPhone(),emailBody);
          }
          catch (Exception e){

          }
            // Add a confirmation message for the user
            model.addAttribute("message",
                    "Your request has been successfully submitted. Our team is currently reviewing your application, "
                            + "and you will receive a notification once your account has been verified. Thank you for your patience."
            );
        }
        return "waiting"; // Redirects to a waiting page
    }

    @GetMapping("/reset")
    public String resetpass(Model model) {
        return "homerestpass";
    }
    @PostMapping("/resetpass")
    public String resetpass(@RequestParam("phone") Long phone, Model model, HttpSession httpSession) {
        int otp = emailService.otp();  // Generate OTP
        var user = userService.getUserDetails(phone);

        if (user.isPresent()) {
            emailService.sendEmail(user.get().getEmail(), "Reset Password ",
                    "Dear User,\n\n"
                            + "Your One-Time Password (OTP) is: " + otp + "\n\n"
                            + "Please use this code to Reset Password The OTP is valid for a limited time only.\n\n"
                            + "If you did not request this, please ignore this email.\n\n"
                            + "Best regards,\n"
                            + "Reva Library Team\n"
                            + "------------------------------\n"
                            + "This is an automated message. Please do not reply.");
            httpSession.setAttribute("otp", otp);
            httpSession.setAttribute("user", user.get());

            return "homepassword";  // Go to password entry page
        }
        return "redirect:/login";  // Redirect if user not found
    }

    @PostMapping("/verifyotp")
    public String verifyotp(@RequestParam("enteredotp") int enteredotp, HttpSession httpSession, Model model) {
        Integer otp = (Integer) httpSession.getAttribute("otp");  // Retrieve OTP

        if (otp == null) {  // Handle missing OTP
            model.addAttribute("error", "Session expired. Please try again.");
            return "redirect:/reset";
        }

        System.out.println("Entered OTP: " + enteredotp + " | Stored OTP: " + otp);

        if (otp.equals(enteredotp)) {
            return "homeverifyotp";
        }

        model.addAttribute("error", "Invalid OTP. Try again.");
        return "homepassword";  // Return to OTP entry page
    }

    @PostMapping("/finalpass")
    public String finalpass(Model model, HttpSession httpSession,@RequestParam("pass")String pass) {
        UserDetails user = (UserDetails) httpSession.getAttribute("user");
        user.setPassword(securityConfig.passwordEncoder().encode(pass));
        userService.update(user);
        httpSession.invalidate();
        return "redirect:/login";
    }
}
