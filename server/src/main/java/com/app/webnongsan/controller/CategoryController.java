package com.app.webnongsan.controller;

import com.app.webnongsan.domain.Category;
import com.app.webnongsan.domain.response.PaginationDTO;
import com.app.webnongsan.service.CategoryService;
import com.app.webnongsan.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
        return ResponseEntity.ok(null);
    }

    @GetMapping("categories")
    @ApiMessage("Get all categories")
    public ResponseEntity<PaginationDTO> getAll(@Filter Specification<Category> spec, Pageable page) {
        return ResponseEntity.ok(this.categoryService.fetchAllCategories(spec, page));
    }
}
