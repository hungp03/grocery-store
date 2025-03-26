package com.store.grocery.controller;

import com.store.grocery.domain.Category;
import com.store.grocery.domain.response.PaginationDTO;
import com.store.grocery.service.CategoryService;
import com.store.grocery.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v2")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("categories")
    @ApiMessage("Create category")
    public ResponseEntity<Category> create(@Valid @RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.categoryService.create(category));
    }

    @PutMapping("categories")
    @ApiMessage("Update category")
    public ResponseEntity<Category> update(@Valid @RequestBody Category category) {
        return ResponseEntity.ok(this.categoryService.update(category));
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
    public ResponseEntity<PaginationDTO> getAll(Pageable pageable) {
        return ResponseEntity.ok(this.categoryService.fetchAllCategories(pageable));
    }
}
