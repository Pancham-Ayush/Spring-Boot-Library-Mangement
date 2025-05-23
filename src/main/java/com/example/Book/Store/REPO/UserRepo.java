package com.example.Book.Store.REPO;

import com.example.Book.Store.Model.UserDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepo extends CrudRepository<UserDetails, Long> {
}
