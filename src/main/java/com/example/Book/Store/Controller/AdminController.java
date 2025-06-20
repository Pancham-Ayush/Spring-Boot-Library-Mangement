package com.example.Book.Store.Controller;

import com.example.Book.Store.EmailService;
import com.example.Book.Store.Model.Book;
import com.example.Book.Store.Model.LendingRecord;
import com.example.Book.Store.Services.BookServices;
import com.example.Book.Store.Services.LedgerService;
import com.example.Book.Store.Services.ApprovalService;
import com.example.Book.Store.Services.UserService;
import com.example.Book.Store.UserLoginConfig.CustomUserDetailsService;
import com.example.Book.Store.Model.UserApproval;
import com.example.Book.Store.Model.UserDetails;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

    @Controller
    @RequestMapping("admin")
    public class AdminController {
        @Autowired
        CustomUserDetailsService userDetailsService;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        EmailService emailService;
        @Autowired
        CustomUserDetailsService customUserDetailsService;
        @Autowired
        ApprovalService approvalService;
        @Autowired
        private BookServices bookServices;
        @Autowired
        private LedgerService ledgerService;
        @Autowired
        private UserService userService;
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

            model.addAttribute("approval", approvalService.all());
            return "adminhome";
        }

        @GetMapping("/add")
        public String showAddBookForm(Model model) {
            model.addAttribute("book", new Book());
            return "add";
        }

        @PostMapping("/add")
        public String saveBook(@ModelAttribute("book") Book book, @RequestParam("img") MultipartFile f, Model model) throws Exception {
            book.setImage(f.getBytes());
            book.setAvailable(book.getTotal());
            bookServices.add(book);
            model.addAttribute("addedBook", book);
            model.addAttribute("base64Image", Base64.getEncoder().encodeToString(book.getImage()));
            return "addsuccess";
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

            System.out.println("Books retrieved: " + allBooks.size());
            model.addAttribute("books", allBooks);
            model.addAttribute("bookImages", bookImages);
            return "adminall";
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
        @GetMapping("/delete")
        public String removeBook(Model model) {
            model.addAttribute("delete",true);
            return "deletesearch";
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
            return "updatesearch";
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
        public String upd(@ModelAttribute Book book, @RequestParam(value = "img" , required = false) MultipartFile f, Model model) throws IOException {
            if(!f.isEmpty()) {
                book.setImage(f.getBytes());
            }
            else {
                Optional<Book> b=bookServices.findById(book.getId());
                if(b.isPresent()) {
                    book.setImage(b.get().getImage());
                    model.addAttribute("base64Image", Base64.getEncoder().encodeToString(book.getImage()));
                }
            }
            bookServices.update(book);
            model.addAttribute("book", book);
            return "updatesucces";
        }

        @GetMapping("/get")
        public String getBookById(Model model){
            model.addAttribute("search",true);

            return "adminsearch";
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
            return "adminresult";
        }


        @GetMapping("/return")
        public String returnBookForm(Model model) {
            model.addAttribute("return", true);
            return "return";  // Show the return form
        }

        @PostMapping("/return")
        public String processReturn(@RequestParam("rid") Long lendingId, Model model) {
            Optional<LendingRecord> optionalRecord = ledgerService.findById(lendingId);

            if (optionalRecord.isPresent()) {
                LendingRecord record = optionalRecord.get();
                Optional<Book> bookOptional = bookServices.findById(record.getBookid());
                if (bookOptional.isPresent()) {
                    model.addAttribute("record", record);
                    model.addAttribute("book", bookOptional.get());
                    // Increase available copies
                    bookOptional.get().setAvailable(bookOptional.get().getAvailable() + 1);
                    bookServices.update(bookOptional.get());
                    // Delete the lending record
                    ledgerService.deletebyId(lendingId);

                    return "returnsuccess";
                } else {
                    ledgerService.deletebyId(lendingId);

                    model.addAttribute("error", "Book not found.");
                    return "/admin"; // Redirect to an error page if book is missing
                }
            } else {
                model.addAttribute("error", "Lending record not found.");
                return "adminerror"; // Redirect to an error page if record is missing
            }
        }
        @GetMapping("/lend")
        public String lendBookForm(Model model) {
            return "adminlend";
        }
        @PostMapping("/lend")
        public String processLend(@RequestParam("bookid") Long bookId, Model model) {
            Optional<Book> book = bookServices.findById(bookId);
            if(book.isPresent() && book.get().getAvailable()>0){
                model.addAttribute("book",book.get());
                LendingRecord lendingRecord=new LendingRecord();
                lendingRecord.setBookName(book.get().getTitle());
                lendingRecord.setBookid(book.get().getId());
                model.addAttribute("record",lendingRecord);
                return "adminlendsuccess";
            }

            return "redirect:/adminerror";
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
                    + "Thank you for using Reva Library.\n\n"
                    + "You have successfully borrowed the book:\n\n"
                    + "Title: " + book.get().getTitle() + "\n"
                    + "Author: " + book.get().getAuthor() + "\n"
                    + "Return Date: " + lendingRecord.getDate() + "\n\n"
                    + "Please ensure that the book is returned by the due date to avoid any late fees.\n\n"
                    + "Happy Reading! 📚\n"
                    + "-- Reva Library Team\n\n"
                    + "----------------------------------------\n"
                    + "This is an automated message. Please do not reply.";



            emailService.sendEmail(lendingRecord.getEmail(), subject, body);
            model.addAttribute("book",book.get());
            model.addAttribute("base64Image",Base64.getEncoder().encodeToString(book.get().getImage()));
            return "adminlendsuccessfull";
        }
        @GetMapping("/lendlist")
        public String lendingList(Model model) {
            List<LendingRecord> lendlist =ledgerService.all(); // Get all lending records
            Map<Long, String> bookImages = new HashMap<>();

            for (LendingRecord record : lendlist) {
                Optional<Book> book = bookServices.findById(record.getBookid());
                book.ifPresent(b -> bookImages.put(b.getId(), Base64.getEncoder().encodeToString(b.getImage()))); // Assuming image is already Base64 encoded
            }

            model.addAttribute("lendlist", lendlist);
            model.addAttribute("bookImages", bookImages);
            return "lendlist";
        }
                @GetMapping("/approval")
                public String approvalForm(Model model) {
                    model.addAttribute("approval", approvalService.all());
                    return "adminapproval";
                }

                @PostMapping("/approval")
                public String approvalProcess(@RequestParam("userId") Long id) {
                    Optional<UserApproval> optionalApproval = Optional.ofNullable(approvalService.findById(id));
                    if (optionalApproval.isPresent()) {
                        UserApproval userApproval = optionalApproval.get();
                        UserDetails userDetails = new UserDetails();
                        userDetails.approvalmatch(userApproval);

                        customUserDetailsService.registerUser(userDetails);
                        emailService.sendEmail(userDetails.getEmail(), "Congratulations!",
                                "Dear Library Member,\n\n"
                                        + "We are delighted to inform you that your request has been approved successfully.\n\n"
                                        + "You may now proceed with borrowing or accessing the requested book(s) at Reva Library.\n\n"
                                        + "Thank you for being a valued member of our community. Happy reading! 📚\n\n"
                                        + "Best regards,\n"
                                        + "The Reva Library Team\n"
                                        + "------------------------------\n"
                                        + "This is an automated message. Please do not reply.");
                        approvalService.rejectUser(userApproval);
                    }
                    else
                        System.out.println("User not found for approval ID: " + id);
                    return "redirect:/admin/approval"; // Fixed redirect path
                }

                @PostMapping("/reject")
                public String approvalReject(@RequestParam("userId") Long id) {
                    Optional<UserApproval> optionalApproval = Optional.ofNullable(approvalService.findById(id));

                    if (optionalApproval.isPresent()) {
                        UserApproval userApproval = optionalApproval.get();
                        emailService.sendEmail(userApproval.getEmail(), "Request Rejected",
                                "We regret to inform you that your request has been rejected.");
                        approvalService.rejectUser(userApproval);
                    } else {
                        System.out.println("User not found for rejection ID: " + id);
                    }

                    return "redirect:/admin/approval"; // Fixed redirect path
                }
            @GetMapping("/edit")
            public String editForm(Model model) {
            return "adminedit";
            }
            @PostMapping("/edit")
        public String editProcess(@RequestParam("pass")String pass,HttpSession httpSession, Model model) {
            UserDetails userd=(UserDetails)httpSession.getAttribute("loggedInUser");
                if (passwordEncoder.matches(pass, userd.getPassword())) {
                    int otp = emailService.otp();
                    httpSession.setAttribute("otp", otp);
                    emailService.sendEmail(userd.getEmail(), "Your OTP Code",
                            "Dear User,\n\n"
                                    + "Your One-Time Password (OTP) is: " + otp + "\n\n"
                                    + "Please use this code to complete your verification. The OTP is valid for a limited time only.\n\n"
                                    + "If you did not request this, please ignore this email.\n\n"
                                    + "Best regards,\n"
                                    + "Reva Library Team\n"
                                    + "------------------------------\n"
                                    + "This is an automated message. Please do not reply.");
                    System.out.println("I am here "+otp);

                    return "editotpform";
                }

                return "redirect:/admin";
            }
            @PostMapping("/editotp")
        public String verifyotpedit(Model model,@RequestParam("otp")int Otp,HttpSession httpSession,@SessionAttribute(value = "otp", required = false) int otp) {
                if(Otp==otp){
                    System.out.println("otp verified");
                    UserDetails userDetails = (UserDetails) httpSession.getAttribute("loggedInUser");
                    model.addAttribute("user", userDetails);
                    return "editform";
                }
                return "redirect:/admin";

            }
        @PostMapping("/editchanges")
        public String editchanges(Model model, HttpSession httpSession, @ModelAttribute("user") UserDetails userDetails) {
            System.out.println("Received User: " + userDetails);  // Debugging


            UserDetails existingUser = (UserDetails) httpSession.getAttribute("loggedInUser");
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());


            httpSession.setAttribute("changes", existingUser);
            httpSession.removeAttribute("otp");

            model.addAttribute("user", userDetails);
            return "admineditchanges";
        }

        @PostMapping("/editfinal")
        public String editfinal(Model model,HttpSession httpSession) {
            UserDetails userDetails = (UserDetails) httpSession.getAttribute("changes");
            httpSession.removeAttribute("changes");
            userService.update(userDetails);
            return "cahngessaved";
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

    }


