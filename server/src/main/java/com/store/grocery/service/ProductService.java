package com.store.grocery.service;

import com.store.grocery.domain.Product;
import com.store.grocery.domain.response.PaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface ProductService {
    Product create(Product p);
    void delete(long id);
    PaginationDTO getAll(Specification<Product> spec, Pageable pageable);
    Product findById(long id);
    Product update(Product p);
    Product updateQuantity(long id, int quantity);
    PaginationDTO search(Specification<Product> spec, Pageable pageable);
    CompletableFuture<byte[]> exportDataToExcelAsync();
}
