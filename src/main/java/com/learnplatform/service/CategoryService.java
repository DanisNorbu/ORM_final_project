package com.learnplatform.service;

import com.learnplatform.entity.Category;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.CategoryRepository;
import com.learnplatform.repository.CourseRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public CategoryService(CategoryRepository categoryRepository, CourseRepository courseRepository) {
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Category create(String name) {
        String normalized = name.trim();
        if (categoryRepository.existsByNameIgnoreCase(normalized)) {
            throw new ConflictException("Category already exists: " + normalized);
        }
        return categoryRepository.save(new Category(normalized));
    }

    @Transactional(readOnly = true)
    public Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Category> list() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category rename(Long id, String newName) {
        Category category = getOrThrow(id);
        String normalized = newName.trim();
        if (!normalized.equalsIgnoreCase(category.getName()) && categoryRepository.existsByNameIgnoreCase(normalized)) {
            throw new ConflictException("Category already exists: " + normalized);
        }
        category.setName(normalized);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getOrThrow(id);
        if (courseRepository.existsByCategoryId(id)) {
            throw new ConflictException("Cannot delete category id=" + id + " because courses reference it");
        }
        categoryRepository.delete(category);
    }
}
