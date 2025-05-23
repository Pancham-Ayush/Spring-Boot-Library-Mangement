

package com.example.Book.Store.Controller;



import com.example.Book.Store.EmailService;
import com.example.Book.Store.Model.UserDetails;
import com.example.Book.Store.Services.UserService;
import com.example.Book.Store.UserLoginConfig.SecurityConfig;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import com.example.Book.Store.Model.Book;
import com.example.Book.Store.Model.LendingRecord;
import com.example.Book.Store.Services.BookServices;
import com.example.Book.Store.Services.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("books")
public class BookController {
    @Autowired
    SecurityConfig securityConfig;
    @Autowired
    private PasswordEncoder passwordEncoder;
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

            Optional<com.example.Book.Store.Model.UserDetails> userOpt =
                    userService.getUserDetails(Long.parseLong(securityUser.getUsername()));

            if (userOpt.isPresent()) {
                com.example.Book.Store.Model.UserDetails user = userOpt.get();
                session.setAttribute("loggedInUser", user);
                model.addAttribute("loggedInUser", user);
            }
        }

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
        lendingRecord.setDate(new Date());
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
    @GetMapping("/takenbooks")
    public String takenBooks(Model model,HttpSession httpSession) {
        UserDetails loggedInUser = (UserDetails) httpSession.getAttribute("loggedInUser");
        List<LendingRecord> taken = ledgerService.findByPhone(loggedInUser.getPhone());
        model.addAttribute("taken", taken);
        return "takenbooks";
    }
    @GetMapping("/returnlist")
    public String returnBookList(Model model, HttpSession httpSession) {
        UserDetails loggedInUser = (UserDetails) httpSession.getAttribute("loggedInUser");

            model.addAttribute("returnedBooks",ledgerService.findByPhone(loggedInUser.getPhone()));


        return "lenduser";
    }
    @GetMapping("/edit")
    public String editForm(Model model) {
        return "useredit";
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

            return "usereditotpform";
        }

        return "redirect:/books";
    }
    @PostMapping("/editotp")
    public String verifyotpedit(Model model,@RequestParam("otp")int Otp,HttpSession httpSession,@SessionAttribute(value = "otp", required = false) int otp) {
        if(Otp==otp){
            System.out.println("otp verified");
            UserDetails userDetails = (UserDetails) httpSession.getAttribute("loggedInUser");
            model.addAttribute("user", userDetails);
            return "usereditform";
        }
        return "redirect:/books";

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
        return "usereditchanges";
    }

    @PostMapping("/editfinal")
    public String editfinal(Model model,HttpSession httpSession) {
        UserDetails userDetails = (UserDetails) httpSession.getAttribute("changes");
        httpSession.removeAttribute("changes");
        userService.update(userDetails);
        return "usercahngessaved";
    }
    @PostMapping("/finalpass")
    public String finalpass(Model model, HttpSession httpSession,@RequestParam("pass")String pass) {
        UserDetails user = (UserDetails) httpSession.getAttribute("user");
        user.setPassword(securityConfig.passwordEncoder().encode(pass));
        userService.update(user);
        httpSession.invalidate();
        return "redirect:/login";
    }
    @GetMapping("/reset")
    public String resetpass(Model model) {
        return "restpass";
    }

    @PostMapping("/resetpass")
    public String resetpass(@RequestParam("phone") Long phone, Model model, HttpSession httpSession) {
        int otp = emailService.otp();  // Generate OTP
        var user = userService.getUserDetails(phone);

        if (user.isPresent()) {
            emailService.sendEmail(user.get().getEmail(), "Reset Password ",
                    "Dear User,\n\n"
                            + "Your One-Time Password (OTP) is: " + otp + "\n\n"
                            + "Please use this code to Reset Password The OTP is valid for a limited time only.\n\n"
                            + "If you did not request this, please ignore this email.\n\n"
                            + "Best regards,\n"
                            + "Reva Library Team\n"
                            + "------------------------------\n"
                            + "This is an automated message. Please do not reply.");
            httpSession.setAttribute("otp", otp);  // ✅ Store OTP in session
            httpSession.setAttribute("user", user.get()); // ✅ Store user in session

            return "password";  // Go to password entry page
        }
        return "redirect:/login";  // Redirect if user not found
    }

    @PostMapping("/verifyotp")
    public String verifyotp(@RequestParam("enteredotp") int enteredotp, HttpSession httpSession, Model model) {
        Integer otp = (Integer) httpSession.getAttribute("otp");  // Retrieve OTP

        if (otp == null) {
            model.addAttribute("error", "Session expired. Please try again.");
            return "redirect:/reset";
        }

        System.out.println("Entered OTP: " + enteredotp + " | Stored OTP: " + otp);

        if (otp.equals(enteredotp)) {
            return "verifyotp";
        }

        model.addAttribute("error", "Invalid OTP. Try again.");
        return "password";  // Return to OTP entry page
    }

}


