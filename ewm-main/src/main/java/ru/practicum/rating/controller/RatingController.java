package ru.practicum.rating.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.rating.dto.NewRating;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.dto.RatingEventDto;
import ru.practicum.rating.service.RatingService;

import javax.validation.Valid;

@RestController()
@RequestMapping("/rating/{userId}")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RatingDto create(@PathVariable Integer userId, @RequestBody @Valid NewRating newRating) {
        log.info("* Запрос Post: добавление рейтинга пользователем с id = {}, рейтинг: {}", userId, newRating);
        return ratingService.create(userId, newRating);
    }

    @DeleteMapping("/{ratingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer userId, @PathVariable Integer ratingId) {
        log.info("* Запрос Delete: удаление пользователем с id = {}, рейтинга с id = {}", userId, ratingId);
        ratingService.delete(userId, ratingId);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public RatingEventDto ratingEvent(@PathVariable Integer userId, @PathVariable Integer eventId) {
        log.info("* Запрос Get: получение пользователем с id = {}, рейтинга для события с id = {}", userId, eventId);
        return ratingService.ratingEvent(userId, eventId);
    }
}
