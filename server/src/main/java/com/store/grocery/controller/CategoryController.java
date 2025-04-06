package com.store.grocery.controller;

import com.store.grocery.domain.Category;
import com.store.grocery.dto.request.category.CreateCategoryRequest;
import com.store.grocery.dto.request.category.UpdateCategoryRequest;
import com.store.grocery.dto.response.PaginationResponse;
import com.store.grocery.service.CategoryService;
import com.store.grocery.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v2")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("categories")
    @ApiMessage("Create category")
    public ResponseEntity<Category> create(@Valid @RequestBody CreateCategoryRequest categoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.categoryService.create(categoryDTO));
    }

    @PutMapping("categories")
    @ApiMessage("Update category")
    public ResponseEntity<Category> update(@Valid @RequestBody UpdateCategoryRequest categoryDTO) {
        return ResponseEntity.ok(this.categoryService.update(categoryDTO));
    }

    @GetMapping("categories/{id}")
    @ApiMessage("Get a category")
    public ResponseEntity<Category> get(@PathVariable("id") long id) {
        Category c = this.categoryService.findById(id);
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("categories/{id}")
    @ApiMessage("Delete category")
    public ResponseEntity<Void> delete(@PathVariable("id") long id){
        this.categoryService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("categories")
    @ApiMessage("Get all categories")
    public ResponseEntity<PaginationResponse> getAll(Pageable pageable) {
        return ResponseEntity.ok(this.categoryService.fetchAllCategories(pageable));
    }
}
