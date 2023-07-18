package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

import javax.validation.Valid;

@RestController()
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CategoryAdminController {

    private final CategoryService categoryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("* Запрос Post: создание категории {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{catId}")
    public void deleteCategoryId(@PathVariable Integer catId) {
        log.info("* Запрос Delete: удаление категории по id = {}", catId);
        categoryService.deleteCategoryId(catId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("{catId}")
    public CategoryDto updateCategory(@PathVariable Integer catId, @RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("* Запрос Patch: обновление категории с id = {}, категория: {}", catId, newCategoryDto);
        return categoryService.updateCategory(catId, newCategoryDto);
    }
}
