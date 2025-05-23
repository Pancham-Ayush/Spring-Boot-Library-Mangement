package com.example.Book.Store.Services;

import com.example.Book.Store.Model.UserDetails;
import com.example.Book.Store.REPO.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
@Configuration
public class UserService {
    @Autowired
    private UserRepo userRepo;

    public void add(UserDetails userDetails) {
        userRepo.save(userDetails);
    }
    public Optional<UserDetails> getUserDetails(Long phone) {
       return userRepo.findById(phone);
    }
    public void update(UserDetails userDetails) {
        userRepo.save(userDetails);
    }
    public boolean contains(Long userDetails) {
       Optional<UserDetails> userDetail = userRepo.findById(userDetails);
    return userDetail.isPresent();}


}
