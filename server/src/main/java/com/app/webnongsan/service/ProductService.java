package com.app.webnongsan.service;

import com.app.webnongsan.domain.Product;
import com.app.webnongsan.domain.response.PaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;

public interface ProductService {
    Product create(Product p);
    void delete(long id);
    PaginationDTO getAll(Specification<Product> spec, Pageable pageable);
    Product findById(long id);
    Product update(Product p);
    Product updateQuantity(long id, int quantity);
    PaginationDTO search(Specification<Product> spec, Pageable pageable);
    byte[] exportDataToExcel() throws IOException;
}
