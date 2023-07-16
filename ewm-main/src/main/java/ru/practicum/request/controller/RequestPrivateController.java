package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
@Slf4j
public class RequestPrivateController {

    private final RequestService requestService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ParticipationRequestDto> requests(@PathVariable Integer userId) {
        log.info("* Запрос Get: получение заявок на участие в событиях пользователем с id = {}", userId);
        return requestService.requests(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ParticipationRequestDto createRequest(@PathVariable Integer userId, @RequestParam Integer eventId) {
        log.info("* Запрос Post: добавления запроса пользователя с id = {}, на участие в событии с id = {}",
                 userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Integer userId, @PathVariable Integer requestId) {
        log.info("* Запрос Patch: отмена участия в событии пользователя с id = {}, запрос id = {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}
