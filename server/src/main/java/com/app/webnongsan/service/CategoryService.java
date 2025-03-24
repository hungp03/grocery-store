package com.app.webnongsan.service;

import com.app.webnongsan.domain.Category;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.repository.CategoryRepository;
import com.app.webnongsan.repository.ProductRepository;
import com.app.webnongsan.util.PaginationHelper;
import com.app.webnongsan.util.exception.CannotDeleteException;
import com.app.webnongsan.util.exception.DuplicateResourceException;
import com.app.webnongsan.util.exception.ResourceInvalidException;
import lombok.AllArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PaginationHelper paginationHelper;

    public boolean isCategoryExisted(String name) {
        return this.categoryRepository.existsByName(name);
    }

    public Category create(Category category) {
        if (this.isCategoryExisted(category.getName())) {
            throw new DuplicateResourceException("Category đã tồn tại");
        }
        return this.categoryRepository.save(category);
    }

    public boolean isCategoryNameUnique(Long id, String name) {
        return !categoryRepository.existsByNameAndIdNot(name, id);
    }

    public Category findById(long id) {
        return this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceInvalidException("Category không tồn tại"));
    }

    public Category update(Category category) {
        if (!this.isCategoryNameUnique(category.getId(), category.getName())) {
            throw new DuplicateResourceException("Category bị trùng");
        }
        Category curr = this.findById(category.getId());
        curr.setName(category.getName());
        curr.setImageUrl(category.getImageUrl());
        return categoryRepository.save(curr);
    }

    public void delete(long id) {
        if (productRepository.existsByCategoryId(id)) {
            throw new CannotDeleteException("Không thể xóa vì category có sản phẩm liên quan.");
        }
        categoryRepository.deleteById(id);
    }

    public PaginationDTO fetchAllCategories(Pageable pageable) {
        return paginationHelper.fetchAllEntities(pageable, categoryRepository);
    }
}
