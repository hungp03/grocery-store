package com.store.grocery.service.impl;

import com.store.grocery.domain.Category;
import com.store.grocery.domain.Product;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.product.ProductResponse;
import com.store.grocery.dto.response.product.SearchProductResponse;
import com.store.grocery.mapper.ProductMapper;
import com.store.grocery.repository.CategoryRepository;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.service.ProductService;
import com.store.grocery.util.exception.ResourceInvalidException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;
    private final ProductMapper productMapper;

    private boolean checkValidCategoryId(long categoryId) {
        log.debug("Checking if category ID: {} is valid", categoryId);
        return this.categoryRepository.existsById(categoryId);
    }

    @Override
    public boolean hasProductsInCategory(long categoryId) {
        return productRepository.existsByCategoryId(categoryId);
    }

    @Override
    public Product create(Product p) {
        log.info("Creating new product with name: {}", p.getProductName());
        if (!this.checkValidCategoryId(p.getCategory().getId())) {
            throw new ResourceInvalidException("Category không tồn tại");
        }
        Product savedProduct = this.productRepository.save(p);
        log.info("Successfully created product with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    private boolean checkValidProductId(long id) {
        return this.productRepository.existsById(id);
    }

    @Override
    public void delete(long id) {
        log.info("Attempting to delete product with ID: {}", id);
        if (!this.checkValidProductId(id)) {
            throw new ResourceInvalidException("Product id = " + id + " không tồn tại");
        }
        this.productRepository.deleteById(id);
        log.info("Successfully deleted product with ID: {}", id);
    }

    @Override
    public PaginationResponse getAll(Specification<Product> spec, Pageable pageable) {
        log.info("Fetching all products");
        Page<ProductResponse> productPage = this.productRepository.findAll(spec, pageable).map(productMapper::toProductResponse);
        log.info("Successfully fetched all products");
        return PaginationResponse.from(productPage, pageable);
    }

    @Override
    public Product findById(long id) {
        log.debug("Fetching product by ID: {}", id);
        return this.productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", id);
                    return new ResourceInvalidException("Product id = " + id + " không tồn tại");
                });
    }

    @Override
    public Product update(Product p) {
        log.info("Updating product with ID: {}", p.getId());
        Product prod = this.findById(p.getId());
        prod.setProductName(p.getProductName());
        prod.setPrice(p.getPrice());
        prod.setImageUrl(p.getImageUrl());
        prod.setDescription(p.getDescription());
        prod.setQuantity(p.getQuantity());
        prod.setUnit(p.getUnit());
        if (p.getCategory() != null) {
            Category category = this.categoryRepository.findById(p.getCategory().getId())
                    .orElseThrow(() -> new ResourceInvalidException("Category id = " + p.getCategory().getId() + " không tồn tại"));
            prod.setCategory(category);
        }
        Product updatedProduct = this.productRepository.save(prod);
        log.info("Successfully updated product with ID: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Override
    public Product updateQuantity(long id, int quantity) {
        Product p = this.findById(id);

        if (quantity > p.getQuantity()) {
            throw new ResourceInvalidException("Product id = " + id + " không đủ số lượng tồn kho");
        }

        p.setQuantity(p.getQuantity() - quantity);
        p.setSold(p.getSold() + quantity);
        return this.productRepository.save(p);
    }

    @Override
    public PaginationResponse search(Specification<Product> spec, Pageable pageable) {
        log.info("Searching for products");
        return PaginationResponse.from(this.searchProduct(spec, pageable), pageable);
    }

    private Page<SearchProductResponse> searchProduct(Specification<Product> specification, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SearchProductResponse> query = cb.createQuery(SearchProductResponse.class);
        Root<Product> productRoot = query.from(Product.class);
        Join<Product, Category> categoryJoin = productRoot.join("category");
        Predicate predicate = specification.toPredicate(productRoot, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        query.select(cb.construct(SearchProductResponse.class,
                productRoot.get("id"),
                productRoot.get("productName"),
                productRoot.get("price"),
                productRoot.get("imageUrl"),
                categoryJoin.get("slug")
        ));

        List<SearchProductResponse> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        countQuery.select(cb.count(countRoot));

        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, cb);
        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(resultList, pageable, totalCount);
    }

    @Async
    @Override
    public CompletableFuture<byte[]> exportDataToExcelAsync() {
        log.info("Exporting data to Excel asynchronously");
        return CompletableFuture.supplyAsync(() -> {
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Products");

                // Tiêu đề cột
                List<String> headers = List.of("ID", "Name", "Image", "Quantity", "Price", "Sold", "Unit", "Rating", "Description");
                createHeaderRow(sheet, headers);

                // Phân trang và lấy dữ liệu từ database
                Pageable pageable = PageRequest.of(0, 100);
                Page<Product> productPage;
                int rowIndex = 1;

                do {
                    productPage = productRepository.findAll(pageable);
                    for (Product p : productPage) {
                        Row row = sheet.createRow(rowIndex++);
                        populateProductRow(row, p);
                    }
                    pageable = pageable.next();
                } while (productPage.hasNext());  // Lặp lại nếu còn trang tiếp theo

                workbook.write(outputStream);
                return outputStream.toByteArray();
            } catch (IOException e) {
                log.error("Error creating Excel file: ", e);
                throw new RuntimeException("Lỗi khi tạo file Excel", e);
            }
        });
    }

    private void createHeaderRow(Sheet sheet, List<String> headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }
    }

    private void populateProductRow(Row row, Product p) {
        row.createCell(0).setCellValue(p.getId());
        row.createCell(1).setCellValue(p.getProductName());
        row.createCell(2).setCellValue(p.getImageUrl());
        row.createCell(3).setCellValue(p.getQuantity());
        row.createCell(4).setCellValue(p.getPrice());
        row.createCell(5).setCellValue(p.getSold());
        row.createCell(6).setCellValue(p.getUnit());
        row.createCell(7).setCellValue(p.getRating());
        row.createCell(8).setCellValue(p.getDescription());
    }

}
