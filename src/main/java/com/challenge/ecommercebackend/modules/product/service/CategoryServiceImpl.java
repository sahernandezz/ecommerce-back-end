package com.challenge.ecommercebackend.modules.product.service;

import com.challenge.ecommercebackend.modules.product.persisten.entity.Category;
import com.challenge.ecommercebackend.modules.product.persisten.entity.CategoryStatus;
import com.challenge.ecommercebackend.modules.product.persisten.repository.ICategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private final ICategoryRepository categoryRepository;

    public CategoryServiceImpl(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategoriesActive() {
        return categoryRepository.findAllByStatus(CategoryStatus.ACTIVE);
    }
}
