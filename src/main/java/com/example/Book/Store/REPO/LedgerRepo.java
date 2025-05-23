package com.example.Book.Store.REPO;

import com.example.Book.Store.Model.LendingRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LedgerRepo extends CrudRepository<LendingRecord,Integer> {
    @Query(value = "SELECT * FROM record WHERE phone = :phone", nativeQuery = true)
    List<LendingRecord> findByPhone(Long phone);



}
