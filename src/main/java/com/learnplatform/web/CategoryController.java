package com.learnplatform.web;

import com.learnplatform.entity.Category;
import com.learnplatform.service.CategoryService;
import com.learnplatform.web.dto.CategoryCreateRequest;
import com.learnplatform.web.dto.CategoryPatchRequest;
import com.learnplatform.web.dto.CategoryView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryView> create(@Valid @RequestBody CategoryCreateRequest req) {
        Category created = categoryService.create(req.name());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(created));
    }

    @GetMapping
    public List<CategoryView> list() {
        return categoryService.list().stream().map(CategoryController::toView).toList();
    }

    @GetMapping("/{id}")
    public CategoryView get(@PathVariable @Positive Long id) {
        return toView(categoryService.getOrThrow(id));
    }

    @PatchMapping("/{id}")
    public CategoryView rename(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CategoryPatchRequest req
    ) {
        return toView(categoryService.rename(id, req.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static CategoryView toView(Category c) {
        return new CategoryView(c.getId(), c.getName());
    }
}
