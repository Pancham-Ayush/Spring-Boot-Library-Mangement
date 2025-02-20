package com.example.Book.Store.Services;


import com.example.Book.Store.Book;
import com.example.Book.Store.REPO.BookRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookServices {

    @Autowired
    private BookRepo bookRepo;

        public List<Book> findAll() {
             return (List<Book>) bookRepo.findAll();
        }

    public Optional<Book> findById(Long id) {

        return bookRepo.findById(Math.toIntExact(id));
    }

    public void add(Book book) {
        bookRepo.save(book);
    }

    public void deleteById(Long id) {

            bookRepo.deleteById(Math.toIntExact(id));
    }

    public void update(Book book) {

            bookRepo.save(book);
    }
    public List<Book> searchBooks(String search){
        return bookRepo.searchBooks(search);
    }
}
