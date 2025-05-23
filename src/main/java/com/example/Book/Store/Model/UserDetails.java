package com.example.Book.Store.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "UserDetails")
public class UserDetails {

    @Id
    @Column(name = "phone", unique = true)
    private Long phone;
    @Column(name = "password")
    private String password;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "role")
    private String role;

    public void approvalmatch(UserApproval user) {
        this.phone=user.getPhone();
        this.name = user.getName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
    }
}
