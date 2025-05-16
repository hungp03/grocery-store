package com.store.grocery.service.impl;

import com.store.grocery.domain.Category;
import com.store.grocery.domain.Product;
import com.store.grocery.dto.request.product.ProductRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.dto.response.product.ProductResponse;
import com.store.grocery.dto.response.product.SearchProductResponse;
import com.store.grocery.mapper.ProductMapper;
import com.store.grocery.repository.CategoryRepository;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.service.ProductService;
import com.store.grocery.util.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    public Product create(ProductRequest productRequest) {
        log.info("Creating new product with name: {}", productRequest.getProductName());
        if (!this.checkValidCategoryId(productRequest.getCategory().getId())) {
            throw new ResourceNotFoundException("Category không tồn tại");
        }
        Category category = Category.builder()
                .id(productRequest.getCategory().getId())
                .build();

        Product product = Product.builder()
                .productName(productRequest.getProductName())
                .price(productRequest.getPrice())
                .imageUrl(productRequest.getImageUrl())
                .quantity(productRequest.getQuantity())
                .description(productRequest.getDescription())
                .unit(productRequest.getUnit())
                .category(category).build();
        Product savedProduct = this.productRepository.save(product);
        log.info("Successfully created product with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public void delete(long id) {
        log.info("Attempting to soft delete product with ID: {}", id);
        Product product = findByIdAndIsActiveTrue(id);
        product.setActive(false);
        this.productRepository.save(product);
        log.info("Successfully marked product with ID: {} as inactive", id);
    }

    @Override
    public PaginationResponse getAll(Specification<Product> spec, Pageable pageable) {
        log.info("Fetching all products");
        Specification<Product> isActiveOnly = (root, query, cb) -> cb.isTrue(root.get("isActive"));
        Specification<Product> productSpecification = isActiveOnly.and(spec);
        Page<ProductResponse> productPage = this.productRepository.findAll(productSpecification, pageable).map(productMapper::toProductResponse);
        log.info("Successfully fetched all products");
        return PaginationResponse.from(productPage, pageable);
    }

    @Override
    public Product findById(long id) {
        log.debug("Fetching product by ID: {}", id);
        return this.productRepository.findById(id).orElseThrow(() -> {
            log.error("Product not found with ID: {}", id);
            return new ResourceNotFoundException("Product id = " + id + " không tồn tại");
        });
    }

    @Override
    @CacheEvict(value = "products", key = "#id")
    public Product update(long id, ProductRequest productRequest) {
        log.info("Updating product with ID: {}", id);
        Product prod = this.findByIdAndIsActiveTrue(id);
        Category updatedCategory = (productRequest.getCategory() != null) ? this.categoryRepository.findById(productRequest.getCategory().getId()).orElseThrow(() -> new ResourceNotFoundException("Category id = " + productRequest.getCategory().getId() + " không tồn tại")) : prod.getCategory();
        Product updatedProduct = Product.builder().
                id(prod.getId()).
                productName(productRequest.getProductName())
                .price(productRequest.getPrice()).
                imageUrl(productRequest.getImageUrl()).
                description(productRequest.getDescription()).
                quantity(productRequest.getQuantity()).
                unit(productRequest.getUnit()).
                category(updatedCategory).isActive(true).build();
        Product saved = this.productRepository.save(updatedProduct);
        log.info("Successfully updated product with ID: {}", updatedProduct.getId());
        return saved;
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
        Predicate isActivePredicate = cb.isTrue(productRoot.get("isActive"));
        if (predicate != null) {
            query.where(cb.and(predicate, isActivePredicate));
        } else {
            query.where(isActivePredicate);
        }

        query.select(cb.construct(
                SearchProductResponse.class,
                productRoot.get("id"),
                productRoot.get("productName"),
                productRoot.get("price"),
                productRoot.get("imageUrl"),
                categoryJoin.get("slug")
        ));

        List<SearchProductResponse> resultList = entityManager.createQuery(query).setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);

        Predicate countPredicate = specification.toPredicate(countRoot, countQuery, cb);
        Predicate isActiveCountPredicate = cb.isTrue(countRoot.get("isActive"));

        if (countPredicate != null) {
            countQuery.where(cb.and(countPredicate, isActiveCountPredicate));
        } else {
            countQuery.where(isActiveCountPredicate);
        }

        countQuery.select(cb.count(countRoot));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, totalCount);
    }

    @Async
    @Override
    public CompletableFuture<byte[]> exportDataToExcel() {
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
                } while (productPage.hasNext());

                workbook.write(outputStream);
                return outputStream.toByteArray();
            } catch (IOException e) {
                log.error("Error creating Excel file: ", e);
                throw new RuntimeException("Lỗi khi tạo file Excel", e);
            }
        });
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public Product findByIdAndIsActiveTrue(long id) {
        return productRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh"));
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
