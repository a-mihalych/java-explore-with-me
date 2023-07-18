package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.error.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> categories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                                 .map(CategoryMapper::toCategoryDto)
                                 .collect(Collectors.toList());
    }

    @Override
    public CategoryDto categoryById(Integer catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена категория с id = %d", catId));
        });
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public void deleteCategoryId(Integer catId) {
        categoryRepository.findById(catId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена категория с id = %d", catId));
        });
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Integer catId, NewCategoryDto newCategoryDto) {
        categoryRepository.findById(catId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена категория с id = %d", catId));
        });
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(catId, newCategoryDto)));
    }
}
