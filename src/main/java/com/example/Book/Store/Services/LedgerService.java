package com.example.Book.Store.Services;

import com.example.Book.Store.Model.LendingRecord;
import com.example.Book.Store.REPO.LedgerRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class LedgerService  {
    private static final Logger logger = LoggerFactory.getLogger(LedgerService.class);

    @Autowired
    private LedgerRepo ledgerRepo;
    public List<LendingRecord> all(){
        return (List<LendingRecord>) ledgerRepo.findAll();
    }
    public void deletebyId(Long id){
        ledgerRepo.deleteById(Math.toIntExact(id));
    }
    public void add(LendingRecord lendingRecord)
    {
        ledgerRepo.save(lendingRecord);
    }
    public List<LendingRecord> All(){
        return (List<LendingRecord>) ledgerRepo.findAll();
    }
    public Optional<LendingRecord> findById(Long id){
        return ledgerRepo.findById(Math.toIntExact(id));
    }
    public List<LendingRecord> findByPhone(Long phone) {
        List<LendingRecord> records = ledgerRepo.findByPhone(phone);
        logger.info("Records found for phone {}: {}", phone, records.size());
        return records;
    }

}
