package com.example.Book.Store.REPO;

import com.example.Book.Store.Model.UserApproval;
import org.springframework.data.repository.CrudRepository;

public interface ApprovalRepo extends CrudRepository<UserApproval, Long> {
}
