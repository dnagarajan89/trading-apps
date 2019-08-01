package com.spring.cloud.gcp.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.cloud.gcp.data.Book;
import com.spring.cloud.gcp.data.BookStoreRepository;

@RestController
@RequestMapping("data-api")
public class DataEnpoint {

	@Autowired
	private BookStoreRepository bookStoreRepository;
	
	@GetMapping
	public Long testDatastore() {
		Book book = new Book();
		book.setName("Harry potter");
		book.setAuthor("JK Rowling");
		Book bookFromRepo = bookStoreRepository.save(book);
		return bookFromRepo.getId();
	}
	
}
