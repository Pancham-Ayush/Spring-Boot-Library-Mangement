package com.example.Book.Store.Model;

import jakarta.persistence.*;
import lombok.Data;

    @Data
    @Entity
    @Table(name = "user_approval")
    public class UserApproval {

        @Id
        @Column(name = "phone")
        private Long phone;
        @Column(name = "password")
        private String password;
        @Column(name = "name")
        private String name;
        @Column(name = "email")
        private String email;
        @Column(name = "role")
        private String role;



    }
