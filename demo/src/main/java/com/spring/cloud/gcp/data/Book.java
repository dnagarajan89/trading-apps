package com.spring.cloud.gcp.data;

import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity;
import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Book {

	@Id
	private Long id;
	
	private String name;
	
	private String author;
	
}


