package com.example.Book.Store.REPO;


import com.example.Book.Store.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookRepo extends CrudRepository<Book, Integer> {
    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.genre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(b.id AS string) LIKE CONCAT('%', :search, '%')")
    List<Book> searchBooks(String search);



}