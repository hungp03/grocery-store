package com.app.webnongsan.controller;

import com.app.webnongsan.domain.Product;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.service.DataExportService;
import com.app.webnongsan.service.ProductService;
import com.app.webnongsan.util.annotation.ApiMessage;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/v2")
public class ProductController {
    private final ProductService productService;
    private final DataExportService dataExportService;

    public ProductController(ProductService productService, DataExportService dataExportService) {
        this.productService = productService;
        this.dataExportService = dataExportService;
    }

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
        return ResponseEntity.ok(null);
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

    @GetMapping("products/max-price")
    @ApiMessage("Get max price")
    public ResponseEntity<Double> getMaxPrice(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "productName", required = false) String productName){
        return ResponseEntity.ok(this.productService.getMaxPrice(category, productName));
    }

    @GetMapping("products/search")
    @ApiMessage("Search products")
    public ResponseEntity<PaginationDTO> search(@Filter Specification<Product> spec, Pageable pageable) {
        return ResponseEntity.ok(this.productService.search(spec, pageable));
    }
    @PutMapping("products/quantity/{id}")
    @ApiMessage("Update quantity product")
    public ResponseEntity<Product> updateQuantity(@PathVariable("id") long id, @RequestParam("quantity") int quantity) {

        return ResponseEntity.ok(this.productService.updateQuantity(id, quantity));
    }

    @GetMapping("products/exportExcel")
    @ApiMessage("Export product data to excel")
    public ResponseEntity<byte[]> exportProductDataToExcel() throws IOException {
        byte[] excelData = dataExportService.exportDataToExcel();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_data.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelData);
    }
}
