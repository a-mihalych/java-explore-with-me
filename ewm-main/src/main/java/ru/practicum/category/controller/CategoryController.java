package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController()
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> categories(@RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        log.info("* Запрос Get: получение списка категорий, from: {}, size: {}", from, size);
        return categoryService.categories(from, size);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{catId}")
    public CategoryDto categoryById(@PathVariable Integer catId) {
        log.info("* Запрос Get: получение категории по id = {}", catId);
        return categoryService.categoryById(catId);
    }
}
