package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
@Slf4j
public class CompilationController {

    private final CompilationService compilationService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<CompilationDto> compilations(@RequestParam(required = false) Boolean pinned,
                                             @RequestParam(defaultValue = "0") Integer from,
                                             @RequestParam(defaultValue = "10") Integer size) {
        log.info("* Запрос Get: получение подборок событий, (закрепленные/не закрепленные): {}, from: {}, size: {}",
                 pinned, from, size);
        return compilationService.compilations(pinned, from, size);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{compId}")
    public CompilationDto compilationById(@PathVariable Integer compId) {
        log.info("* Запрос Get: получение подборок событий по id = {}", compId);
        return compilationService.compilationById(compId);
    }
}
