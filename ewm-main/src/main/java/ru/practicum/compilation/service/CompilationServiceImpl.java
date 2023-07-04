package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.stats.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final Stats stats;

    @Override
    public List<CompilationDto> compilations(Boolean pinned, Integer from, Integer size) {
        return compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size)).stream()
                .map(compilation -> CompilationMapper.toCompilationDto(compilation, compilation.getEvents().stream()
                        .map(event -> EventMapper.toEventShortDto(event,
                                requestRepository.countRequestConfirmed(event.getId()),
                                Math.toIntExact(stats.hits("2000-01-01 00:00:00",
                                        "3000-01-01 00:00:00",
                                        List.of("/users/{userId}/events"),
                                        true).get(0).getHits())))
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto compilationById(Integer compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена подборка с id = %d", compId));
        });
        List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        Math.toIntExact(stats.hits("2000-01-01 00:00:00",
                                "3000-01-01 00:00:00",
                                List.of("/users/{userId}/events"),
                                true).get(0).getHits())))
                .collect(Collectors.toList());
        return CompilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null) {
            events = newCompilationDto.getEvents().stream()
                    .map(id -> {
                        return eventRepository.findById(id).orElseThrow(() -> {
                            throw new NotFoundException(String.format("Не найдено события с id = %d", id));
                        });
                    })
                    .collect(Collectors.toList());
        }
        Compilation compilation = compilationRepository.save(CompilationMapper.toCompilation(newCompilationDto, events));
        List<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        Math.toIntExact(stats.hits("2000-01-01 00:00:00",
                                "3000-01-01 00:00:00",
                                List.of("/events/" + event.getId()),
                                true).get(0).getHits())))
                .collect(Collectors.toList());
        return CompilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    @Override
    @Transactional
    public void deleteCompilationId(Integer compId) {
        compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена подборка с id = %d", compId));
        });
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Integer compId, UpdateCompilationRequest updateCompilationRequest) {
        compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена подборка с id = %d", compId));
        });
        List<Event> events = new ArrayList<>();
        if (updateCompilationRequest.getEvents() != null) {
            events = updateCompilationRequest.getEvents().stream()
                    .map(id -> {
                        return eventRepository.findById(id).orElseThrow(() -> {
                            throw new NotFoundException(String.format("Не найдено события с id = %d", id));
                        });
                    })
                    .collect(Collectors.toList());
        }
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        Math.toIntExact(stats.hits("2000-01-01 00:00:00",
                                "3000-01-01 00:00:00",
                                List.of("/users/{userId}/events"),
                                true).get(0).getHits())))
                .collect(Collectors.toList());
        return CompilationMapper.toCompilationDto(compilationRepository
                .save(CompilationMapper.toCompilation(compId, updateCompilationRequest, events)), eventShortDtos);
    }
}
