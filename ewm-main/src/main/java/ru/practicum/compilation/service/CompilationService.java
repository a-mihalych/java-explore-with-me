package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> compilations(Boolean pinned, Integer from, Integer size);

    CompilationDto compilationById(Integer compId);

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilationId(Integer compId);

    CompilationDto updateCompilation(Integer compId, UpdateCompilationRequest updateCompilationRequest);
}
