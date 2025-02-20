

package com.example.Book.Store.Controller;


import org.springframework.ui.Model;
import com.example.Book.Store.Book;
import com.example.Book.Store.Services.BookServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("books")
public class BookController {

    @Autowired
    private BookServices bookServices;
    @GetMapping()
    public String home(Model model){
        return "home";
    }
    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "add";
    }

    @PostMapping("/add")
    public String saveBook(@ModelAttribute("book") Book book, @RequestParam("img") MultipartFile f, Model model) throws Exception {
        if (!f.isEmpty()) {
            String base64Image = Base64.getEncoder().encodeToString(f.getBytes());
            book.setImage(base64Image);
        }

        book.setAvailable(book.getTotal());
        bookServices.add(book);
        model.addAttribute("addedBook", book);

        return "addsuccess";
    }




    @GetMapping("/all")
    public String getAllBooks(Model model) {
        List<Book> allBooks=bookServices.findAll();

        List<String>img=new ArrayList<>();
        for(Book b:allBooks){
            if(b.getImage()!=null){
                String base64Image = b.getImage();
                img.add(base64Image);
            }
            else
                img.add("noimage");
        }
        model.addAttribute("books",allBooks);
        model.addAttribute("img",img);
        return "all";
    }
    @GetMapping("/delete")
    public String removeBook(Model model) {
        model.addAttribute("delete",true);
        return "search";
    }
    @PostMapping("delete")
    public String deleteBook(@RequestParam("id")Long id ,Model model) {
        Optional<Book> book=bookServices.findById(id);
        if(book.isPresent()) {
            bookServices.deleteById(id);
            model.addAttribute("deleted",true);
            model.addAttribute("book",book.get());
        }
        else
            model.addAttribute("absent",true);
        return "deletesuccess";
    }
    @GetMapping("/update")
    public String updateBook(Model model) {
        model.addAttribute("update", true);
        return "search";
    }

    @PostMapping("/update")
    public String updateBook(@RequestParam("id") Long id, Model model) {
        Optional<Book> book = bookServices.findById(id);
        if (book.isPresent()) {
            model.addAttribute("present", true);
            model.addAttribute("book", book.get());
        } else
            model.addAttribute("absent", true);
        return "update";

    }

    @PostMapping("/up")
    public String upd(@ModelAttribute Book book,@RequestParam(value = "img" , required = false)MultipartFile f, Model model) throws IOException {
        if(!f.isEmpty()) {
            String base64Image = Base64.getEncoder().encodeToString(f.getBytes());
            model.addAttribute("base64Image", base64Image);
            book.setImage(base64Image);
        }
        else {
            Optional<Book> b=bookServices.findById(book.getId());
            if(b.isPresent()) {
                book.setImage(b.get().getImage());

                model.addAttribute("base64Image", b.get().getImage());
            }
        }

        bookServices.update(book);

        model.addAttribute("book", book);
        return "updatesucces";
    }

    @GetMapping("/get")
    public String getBookById(Model model){
        model.addAttribute("search",true);

        return "search";
    }

    @PostMapping("/get")
    public String searchBooks(@RequestParam("search") String search, Model model) {
        List<Book> books=bookServices.searchBooks(search);

        model.addAttribute("books",books);

        return "result";
    }
    @GetMapping("/lend")
    public String lendBook(Model model){
        model.addAttribute("lend",true);
        return "lend";
    }
    @PostMapping("/lend")
    public void lendBook(@RequestParam("id") Long id, Model model) {
      Optional<Book> book=bookServices.findById(id);
      if(book.isPresent()&&book.get().getAvailable()>0){
          book.get().setAvailable(book.get().getAvailable()-1);
          bookServices.update(book.get());
      }
    }
}

