package com.store.grocery.service.impl;

import com.store.grocery.domain.Category;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.repository.CategoryRepository;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.service.CategoryService;
import com.store.grocery.util.PaginationHelper;
import com.store.grocery.util.exception.CannotDeleteException;
import com.store.grocery.util.exception.DuplicateResourceException;
import com.store.grocery.util.exception.ResourceInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PaginationHelper paginationHelper;

    @Override
    public boolean isCategoryExisted(String name) {
        return this.categoryRepository.existsByName(name);
    }

    @Override
    public Category create(Category category) {
        log.info("Creating new category: {}", category.getName());
        if (this.isCategoryExisted(category.getName())) {
            log.warn("Category '{}' already exists", category.getName());
            throw new DuplicateResourceException("Category đã tồn tại");
        }
        log.info("Category '{}' created successfully", category.getName());
        return this.categoryRepository.save(category);
    }

    @Override
    public boolean isCategoryNameUnique(Long id, String name) {
        log.info("Checking if category '{}' exists", name);
        return !categoryRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public Category findById(long id) {
        log.info("Fetching category with ID {}", id);
        return this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceInvalidException("Category không tồn tại"));
    }

    @Override
    public Category update(Category category) {
        log.info("Updating category ID {} with new name '{}'", category.getId(), category.getName());
        if (!this.isCategoryNameUnique(category.getId(), category.getName())) {
            log.warn("Category name '{}' is not unique for ID {}", category.getName(), category.getId());
            throw new DuplicateResourceException("Category bị trùng");
        }
        Category curr = this.findById(category.getId());
        curr.setName(category.getName());
        curr.setImageUrl(category.getImageUrl());
        log.info("Category ID {} updated successfully", category.getId());
        return categoryRepository.save(curr);
    }

    @Override
    public void delete(long id) {
        log.info("Deleting category with ID {}", id);
        if (productRepository.existsByCategoryId(id)) {
            log.warn("Cannot delete category ID {} because it has associated products", id);
            throw new CannotDeleteException("Không thể xóa vì category có sản phẩm liên quan.");
        }
        categoryRepository.deleteById(id);
        log.info("Category ID {} deleted successfully", id);
    }

    @Override
    public PaginationDTO fetchAllCategories(Pageable pageable) {
        log.info("Fetching all categories with pagination");
        return paginationHelper.fetchAllEntities(pageable, categoryRepository);
    }
}
