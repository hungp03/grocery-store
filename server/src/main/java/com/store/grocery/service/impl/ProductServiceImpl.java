package com.store.grocery.service.impl;

import com.store.grocery.domain.Category;
import com.store.grocery.domain.Product;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.domain.response.product.ResProductDTO;
import com.store.grocery.domain.response.product.SearchProductDTO;
import com.store.grocery.repository.CategoryRepository;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.service.ProductService;
import com.store.grocery.util.PaginationHelper;
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
    private final PaginationHelper paginationHelper;

    private boolean checkValidCategoryId(long categoryId) {
        log.debug("Checking if category ID: {} is valid", categoryId);
        return this.categoryRepository.existsById(categoryId);
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
    public PaginationDTO getAll(Specification<Product> spec, Pageable pageable) {
        log.info("Fetching all products");
        Page<Product> productPage = this.productRepository.findAll(spec, pageable);
        PaginationDTO p = new PaginationDTO();
        PaginationDTO.Meta meta = new PaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(productPage.getTotalPages());
        meta.setTotal(productPage.getTotalElements());
        p.setMeta(meta);
        List<ResProductDTO> listProducts = productPage.getContent().stream().map(this::convertToProductDTO).toList();
        p.setResult(listProducts);
        log.info("Successfully fetched all products");
        return p;
    }

    private ResProductDTO convertToProductDTO(Product p) {
        log.debug("Converting Product to ResProductDTO for product ID: {}", p.getId());
        ResProductDTO res = new ResProductDTO();
        res.setId(p.getId());
        res.setProduct_name(p.getProductName());
        res.setCategory(p.getCategory().getSlug());
        res.setPrice(p.getPrice());
        res.setSold(p.getSold());
        res.setQuantity(p.getQuantity());
        res.setImageUrl(p.getImageUrl());
        res.setUnit(p.getUnit());
        res.setDescription(p.getDescription());
        res.setRating(p.getRating());
        return res;
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
        Product curr = this.findById(p.getId());
        if (curr == null) {
            throw new ResourceInvalidException("Product id = " + p.getId() + " không tồn tại");
        }
        curr.setProductName(p.getProductName());
        curr.setPrice(p.getPrice());
        curr.setImageUrl(p.getImageUrl());
        curr.setDescription(p.getDescription());
        curr.setQuantity(p.getQuantity());
        curr.setUnit(p.getUnit());
        Product updatedProduct = this.productRepository.save(curr);
        log.info("Successfully updated product with ID: {}", updatedProduct.getId());
        return updatedProduct;
    }

    @Override
    public Product updateQuantity(long id, int quantity) {
        Product p = this.productRepository.findById(id)
                .orElseThrow(() -> new ResourceInvalidException("Product id = " + id + " không tồn tại"));

        if (quantity > p.getQuantity()) {
            throw new ResourceInvalidException("Product id = " + id + " không đủ số lượng tồn kho");
        }

        p.setQuantity(p.getQuantity() - quantity);
        p.setSold(p.getSold() + quantity);
        return this.productRepository.save(p);
    }

    @Override
    public PaginationDTO search(Specification<Product> spec, Pageable pageable) {
        log.info("Searching for products");
        Page<SearchProductDTO> productPage = this.searchProduct(spec, pageable);
        return this.paginationHelper.buildPaginationDTO(productPage);
    }

    private Page<SearchProductDTO> searchProduct(Specification<Product> specification, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SearchProductDTO> query = cb.createQuery(SearchProductDTO.class);
        Root<Product> productRoot = query.from(Product.class);
        Join<Product, Category> categoryJoin = productRoot.join("category");
        Predicate predicate = specification.toPredicate(productRoot, query, cb);
        if (predicate != null) {
            query.where(predicate);
        }

        query.select(cb.construct(SearchProductDTO.class,
                productRoot.get("id"),
                productRoot.get("productName"),
                productRoot.get("price"),
                productRoot.get("imageUrl"),
                categoryJoin.get("name")
        ));

        List<SearchProductDTO> resultList = entityManager.createQuery(query)
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
                List<String> headers = List.of("ID", "Name", "Quantity", "Price", "Sold", "Unit", "Rating", "Description");
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.size(); i++) {
                    headerRow.createCell(i).setCellValue(headers.get(i));
                }

                // Lấy dữ liệu từ database
                List<Product> products = productRepository.findAll();
                int rowIndex = 1;
                for (Product p : products) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(p.getId());
                    row.createCell(2).setCellValue(p.getProductName());
                    row.createCell(3).setCellValue(p.getQuantity());
                    row.createCell(4).setCellValue(p.getPrice());
                    row.createCell(5).setCellValue(p.getSold());
                    row.createCell(6).setCellValue(p.getUnit());
                    row.createCell(7).setCellValue(p.getRating());
                    row.createCell(8).setCellValue(p.getDescription());
                }

                workbook.write(outputStream);
                return outputStream.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi tạo file Excel", e);
            }
        });
    }
}
