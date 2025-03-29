package com.store.grocery.controller;

import com.store.grocery.domain.Product;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.service.ProductService;
import com.store.grocery.util.annotation.ApiMessage;
import com.store.grocery.util.exception.ResourceInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("products")
    @ApiMessage("Create product")
    public ResponseEntity<Product> create(@Valid @RequestBody Product p){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productService.create(p));
    }

    @GetMapping("products/{id}")
    @ApiMessage("Get product")
    public ResponseEntity<Product> get(@PathVariable("id") long id) throws ResourceInvalidException {
        return ResponseEntity.ok(this.productService.findById(id));
    }

    @DeleteMapping("products/{id}")
    @ApiMessage("Delete product")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws ResourceInvalidException {
        this.productService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("products")
    @ApiMessage("Get all products")
    public ResponseEntity<PaginationDTO> getAll(@Filter Specification<Product> spec, Pageable pageable){
        return ResponseEntity.ok(this.productService.getAll(spec, pageable));
    }

    @PutMapping("products")
    @ApiMessage("Update product")
    public ResponseEntity<Product> update(@Valid @RequestBody Product p) {
        return ResponseEntity.ok(this.productService.update(p));
    }

    @GetMapping("products/search")
    @ApiMessage("Search products")
    public ResponseEntity<PaginationDTO> search(@Filter Specification<Product> spec, Pageable pageable) {
        return ResponseEntity.ok(this.productService.search(spec, pageable));
    }

    @GetMapping("products/exportExcel")
    @ApiMessage("Export product data to excel")
    public ResponseEntity<byte[]> exportProductDataToExcel() throws IOException, ExecutionException, InterruptedException {
        byte[] excelData = productService.exportDataToExcelAsync().get();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_data.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }
}
