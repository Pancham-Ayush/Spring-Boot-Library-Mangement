package com.example.Book.Store.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Entity
@Table(name = "record")
public class LendingRecord {
    @Id
    @Column(name = "id" )
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "phone")
    private Long phone;
    @Column(name = "name")
    private String name;
    @Column(name = "bookid")
    private Long bookid;
    @Column(name = "email")
    private String email;
    @Column(name = "bookName")
    private String bookName;
    @Column(name = "date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    @Column(name = "return_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date returnDate;





}
