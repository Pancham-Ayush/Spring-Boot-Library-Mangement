package com.example.Book.Store;

//import com.example.Book.Store.Controller.AiController;
import com.example.Book.Store.UserLoginConfig.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class  BookStoreApplication {

	public static void main(String[] args) {
	//hi
		SpringApplication.run(BookStoreApplication.class, args);

	}
}
