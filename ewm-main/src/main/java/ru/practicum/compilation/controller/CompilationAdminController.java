package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
@Slf4j
public class CompilationAdminController {

    private final CompilationService compilationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompilationDto createCompilation(@RequestBody NewCompilationDto newCompilationDto) {
        log.info("* Запрос Post: создание новой подборки {}", newCompilationDto);
        return compilationService.createCompilation(newCompilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void deleteCompilationId(@PathVariable Integer compId) {
        log.info("* Запрос Delete: удаление подборки по id = {}", compId);
        compilationService.deleteCompilationId(compId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Integer compId,
                                            @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("* Запрос Patch: обновление подборки с id = {}, {}", compId, updateCompilationRequest);
        return compilationService.updateCompilation(compId, updateCompilationRequest);
    }
}
