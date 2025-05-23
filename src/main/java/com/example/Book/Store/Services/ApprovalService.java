package com.example.Book.Store.Services;

import com.example.Book.Store.REPO.ApprovalRepo;
import com.example.Book.Store.Model.UserApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
@Configuration
public class ApprovalService {
    @Autowired
    private ApprovalRepo repo;
    public void approveUser(@ModelAttribute UserApproval user) {
        repo.save(user);
    }
    public void rejectUser(@ModelAttribute UserApproval user) {
        repo.delete(user);
    }
    public List<UserApproval> all() {
        return (List<UserApproval>) repo.findAll();
    }
    public UserApproval findById(Long id) {
        return repo.findById(id).get();
    }
    public boolean contains(Long id) {
        return repo.existsById(id);
    }
}
