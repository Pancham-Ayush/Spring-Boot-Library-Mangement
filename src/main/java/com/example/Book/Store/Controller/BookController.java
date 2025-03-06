

package com.example.Book.Store.Controller;



import com.example.Book.Store.EmailService;
import com.example.Book.Store.Model.UserDetails;
import com.example.Book.Store.Services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import com.example.Book.Store.Model.Book;
import com.example.Book.Store.Model.LendingRecord;
import com.example.Book.Store.Services.BookServices;
import com.example.Book.Store.Services.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("books")
public class BookController {
    @Autowired
    EmailService emailService;
    @Autowired
    UserService userService;
    @Autowired
    private BookServices bookServices;
    @Autowired
    private LedgerService ledgerService;

    @GetMapping()
    public String home(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails securityUser =
                    (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();

            // Fetch user details from the database using the phone number (assuming phone is the username)
            Optional<com.example.Book.Store.Model.UserDetails> userOpt =
                    userService.getUserDetails(Long.parseLong(securityUser.getUsername()));

            if (userOpt.isPresent()) {
                com.example.Book.Store.Model.UserDetails user = userOpt.get();
                session.setAttribute("loggedInUser", user);
                model.addAttribute("loggedInUser", user);
            }
        }

        // Ensure `loggedInUser` is always available to prevent Thymeleaf errors
        if (!model.containsAttribute("loggedInUser")) {
            model.addAttribute("loggedInUser", new com.example.Book.Store.Model.UserDetails());
        }


        return "home";
    }






    @GetMapping("/all")
    public String getAllBooks(Model model) {
        List<Book> allBooks = bookServices.findAll();
        Map<Long, String> bookImages = new HashMap<>();
        for (Book book : allBooks) {
            if (book.getImage() != null) {
                String encodedImage = Base64.getEncoder().encodeToString(book.getImage());
                bookImages.put(book.getId(), encodedImage);
            } else {
                bookImages.put(book.getId(), "noimage");
            }
        }
//        AiController aiController = new AiController();
//        System.out.println(aiController.gemini("hii i am java"));
        model.addAttribute("books", allBooks);
        model.addAttribute("bookImages", bookImages);
        return "all";
    }





    @GetMapping("/get")
    public String getBookById(Model model){
        model.addAttribute("search",true);

        return "search";
    }

    @PostMapping("/get")
    public String searchBooks(@RequestParam("search") String search, Model model) {
        List<Book> books = bookServices.searchBooks(search);
        Map<Long, String> bookImages = new HashMap<>();

        for (Book book : books) {
            String base64Image = (book.getImage() != null) ? Base64.getEncoder().encodeToString(book.getImage()) : "noimage";
            bookImages.put(book.getId(), base64Image);
        }

        model.addAttribute("books", books);
        model.addAttribute("bookImages", bookImages);
        return "result";
    }
    @GetMapping("/lend")
    public String lendBookForm(Model model) {
        return "lend";
    }
    @PostMapping("/lend")
    public String processLend(@RequestParam("bookid") Long bookId, Model model,HttpSession httpSession) {
        UserDetails loggedInUser = (UserDetails) httpSession.getAttribute("loggedInUser");
        Optional<Book> book = bookServices.findById(bookId);
        if(book.isPresent() && book.get().getAvailable()>0){
            model.addAttribute("book",book.get());
            LendingRecord lendingRecord=new LendingRecord();
            System.out.println("Lending record: " + lendingRecord.toString());
            lendingRecord.setPhone(loggedInUser.getPhone());
            lendingRecord.setEmail(loggedInUser.getEmail());
            lendingRecord.setName(loggedInUser.getName());
            lendingRecord.setBookName(book.get().getTitle());
            lendingRecord.setBookid(book.get().getId());
            System.out.println("Lending record: " + lendingRecord.toString());

            model.addAttribute("record",lendingRecord);
            return "lendsuccess";
        }

        return "redirect:/error";
    }
    @PostMapping("/lendend")
    public String processLendend(@ModelAttribute("record")LendingRecord lendingRecord, Model model){
        Optional<Book> book = bookServices.findById(lendingRecord.getBookid());
        book.get().setAvailable(book.get().getAvailable()-1);

        bookServices.update(book.get());
        ledgerService.add(lendingRecord);
        model.addAttribute("lendingrecord",lendingRecord);
        String subject = "Book Issued Successfully – " + book.get().getTitle();
        String body = "Dear " + lendingRecord.getName() + ",\n\n"
                + "You have successfully borrowed the book: " + book.get().getTitle() + ".\n"
                + "Author: " + book.get().getAuthor() + "\n"
                + "Return Date: " + lendingRecord.getDate() + "\n\n"
                + "Please ensure to return the book on time to avoid any penalties.\n\n"
                + "Happy Reading! 📚\n"
                + "Reva Library Team\n"
                + "------------------------------\n"
                + "This is an automated message. Please do not reply.";


        try {
            emailService.sendEmail(lendingRecord.getEmail(), subject, body);
        }
        catch (Exception e) {}
        model.addAttribute("book",book.get());
        model.addAttribute("base64Image",Base64.getEncoder().encodeToString(book.get().getImage()));
        return "lendsuccessfull";
    }
    @GetMapping("/book-availability/{bookId}")
    @ResponseBody
    public Map<String, Object> checkBookAvailability(@PathVariable Long bookId) {
        Optional<Book> b =bookServices.findById(bookId);
        int availableCopies = b.get().getAvailable(); // Fetch from database
        Map<String, Object> response = new HashMap<>();
        response.put("available", availableCopies);
        return response;
    }


    @GetMapping("/availability")
    @ResponseBody
    public List<Map<String, Object>> getBookAvailability() {
        List<Map<String, Object>> availabilityList = new ArrayList<>();
        for (Book book : bookServices.findAll()) {
            Map<String, Object> bookData = new HashMap<>();
            bookData.put("id", book.getId());  // Book ID
            bookData.put("available", book.getAvailable()); // Available copies
            availabilityList.add(bookData);
        }
        return availabilityList;
    }
    @GetMapping("/returnlist")
    public String returnBookList(Model model, HttpSession httpSession) {
        UserDetails loggedInUser = (UserDetails) httpSession.getAttribute("loggedInUser");

            model.addAttribute("returnedBooks",ledgerService.findByPhone(loggedInUser.getPhone()));


        return "lenduser";
    }


}


