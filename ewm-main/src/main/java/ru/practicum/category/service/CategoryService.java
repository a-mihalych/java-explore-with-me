package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> categories(Integer from, Integer size);

    CategoryDto categoryById(Integer catId);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategoryId(Integer catId);

    CategoryDto updateCategory(Integer catId, NewCategoryDto newCategoryDto);
}
