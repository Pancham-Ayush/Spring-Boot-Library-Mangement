package com.example.Book.Store.UserLoginConfig;

import com.example.Book.Store.REPO.UserRepo;
import com.example.Book.Store.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new UsernameNotFoundException("Phone number cannot be empty");
        }

        try {
            Long phoneNumber = Long.valueOf(phone);
            return userRepo.findById(phoneNumber)
                    .map(userDetails -> User.withUsername(String.valueOf(userDetails.getPhone()))
                            .password(userDetails.getPassword())
                            .roles(userDetails.getRole())
                            .build())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid phone number format");
        }
    }


    public void registerUser(com.example.Book.Store.Model.UserDetails userDetails) {
        userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword())); // Now works!
        userRepo.save(userDetails);
    }
}
