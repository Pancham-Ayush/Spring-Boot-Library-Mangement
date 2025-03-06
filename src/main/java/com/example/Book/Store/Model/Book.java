package com.example.Book.Store;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;
    @Column(name = "genre")
    private String genre;
    @Column(name = "author")
    private String author;
    @Lob  // Store as binary data
    @Column(columnDefinition = "LONGBLOB")  // Use LONGBLOB for large images
    private byte[] image;
    @Column(name = "description")
    private String description;
    @Column(name = "total")
    private int total;
    @Column(name = "available")
    private int available;


}
