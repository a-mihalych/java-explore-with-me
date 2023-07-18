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
import ru.practicum.error.exception.ValidationException;
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
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(PageRequest.of(from / size, size)).toList();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size));
        }
        return compilations.stream()
                .map(compilation -> CompilationMapper.toCompilationDto(compilation, compilation.getEvents().stream()
                        .map(event -> EventMapper.toEventShortDto(event,
                                requestRepository.countRequestConfirmed(event.getId()),
                                stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                        List.of("/events/" + event.getId()), true)))
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
                        stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                List.of("/events/" + event.getId()), true)))
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
                        stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                List.of("/events/" + event.getId()), true)))
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
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдена подборка с id = %d", compId));
        });
        if (updateCompilationRequest.getTitle() != null && updateCompilationRequest.getTitle().length() > 50) {
            throw new ValidationException(String.format("Зоголовок '%s' превышает ограничение в 50 символов",
                    updateCompilationRequest.getTitle()));
        }
        List<Event> events = new ArrayList<>();
        if (updateCompilationRequest.getEvents() != null) {
            events = eventRepository.findByIdIn(updateCompilationRequest.getEvents());
        }
        List<EventShortDto> eventShortDtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event,
                        requestRepository.countRequestConfirmed(event.getId()),
                        stats.countHits(Stats.DATE_TIME_MIN, Stats.DATE_TIME_MAX,
                                List.of("/events/" + event.getId()), true)))
                .collect(Collectors.toList());
        if (updateCompilationRequest.getTitle() == null) {
            updateCompilationRequest.setTitle(compilation.getTitle());
        }
        return CompilationMapper.toCompilationDto(compilationRepository
                .save(CompilationMapper.toCompilation(compId, updateCompilationRequest, events)), eventShortDtos);
    }
}
