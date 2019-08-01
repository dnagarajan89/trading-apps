package com.spring.cloud.gcp.data;

import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository;

public interface BookStoreRepository extends DatastoreRepository<Book, Long> {

}
