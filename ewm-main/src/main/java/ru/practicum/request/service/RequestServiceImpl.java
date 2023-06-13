package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exception.ConflictException;
import ru.practicum.error.exception.NotFoundException;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> requests(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найден пользователь с id = %d", userId));
        });
        return requestRepository.findByRequesterId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Integer userId, Integer eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найден пользователь с id = %d", userId));
        });
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найдено событие с id = %d", eventId));
        });
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (event.getEventState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        int numberParticipants = requestRepository
                .findByEventIdAndStatus(event.getId(), EventState.PENDING.name()).size();
        if (event.getParticipantLimit() <= numberParticipants) {
            throw new ConflictException("Запрос на участие не добавлен, достигнут лимит запросов на участие");
        }
        if (requestRepository.findByEventIdAndRequesterId(event.getId(), userId).size() > 0) {
            throw new ConflictException("Нельзя добавить повторный запрос");
        }
        RequestStatus requestStatus = RequestStatus.PENDING;
        if (!event.getRequestModeration()) {
            requestStatus = RequestStatus.CONFIRMED;
        }
        Request request = requestRepository.save(Request.builder()
                                                        .created(LocalDateTime.now())
                                                        .requester(user)
                                                        .event(event)
                                                        .status(requestStatus)
                                                        .build());
        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Integer userId, Integer requestId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найден пользователь с id = %d", userId));
        });
        Request request = requestRepository.findByIdAndRequesterId(userId, requestId).orElseThrow(() -> {
            throw new NotFoundException(String.format("Не найден запрос с id = %d и пользователем с id = %d",
                                                      requestId, userId));
        });
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }
}
