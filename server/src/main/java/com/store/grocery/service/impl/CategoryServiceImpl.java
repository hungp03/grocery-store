package com.store.grocery.service.impl;

import com.store.grocery.domain.Category;
import com.store.grocery.dto.request.category.CreateCategoryRequest;
import com.store.grocery.dto.request.category.UpdateCategoryRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.repository.CategoryRepository;
import com.store.grocery.repository.ProductRepository;
import com.store.grocery.service.CategoryService;
import com.store.grocery.service.ProductService;
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
    private final PaginationHelper paginationHelper;
    private final ProductService productService;

    @Override
    public boolean isCategoryExisted(String name) {
        return this.categoryRepository.existsByName(name);
    }

    @Override
    public Category create(CreateCategoryRequest categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());
        if (this.isCategoryExisted(categoryDTO.getName())) {
            log.warn("Category '{}' already exists", categoryDTO.getName());
            throw new DuplicateResourceException("Category đã tồn tại");
        }
        log.info("Category '{}' created successfully", categoryDTO.getName());
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setImageUrl(categoryDTO.getImageUrl());
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
    public Category update(UpdateCategoryRequest categoryDTO) {
        Category curr = this.findById(categoryDTO.getId());
        log.info("Updating category ID {} with new name '{}'", categoryDTO.getId(), categoryDTO.getName());
        if (!this.isCategoryNameUnique(categoryDTO.getId(), categoryDTO.getName())) {
            log.warn("Category name '{}' is not unique for ID {}", categoryDTO.getName(), categoryDTO.getId());
            throw new DuplicateResourceException("Category bị trùng");
        }
        curr.setName(categoryDTO.getName());
        curr.setImageUrl(categoryDTO.getImageUrl());
        log.info("Category ID {} updated successfully", categoryDTO.getId());
        return categoryRepository.save(curr);
    }

    @Override
    public void delete(long id) {
        log.info("Deleting category with ID {}", id);
        if (productService.hasProductsInCategory(id)) {
            log.warn("Cannot delete category ID {} because it has associated products", id);
            throw new CannotDeleteException("Không thể xóa vì category có sản phẩm liên quan.");
        }
        categoryRepository.deleteById(id);
        log.info("Category ID {} deleted successfully", id);
    }

    @Override
    public PaginationResponse fetchAllCategories(Pageable pageable) {
        log.info("Fetching all categories with pagination");
        return paginationHelper.fetchAllEntities(pageable, categoryRepository);
    }
}
