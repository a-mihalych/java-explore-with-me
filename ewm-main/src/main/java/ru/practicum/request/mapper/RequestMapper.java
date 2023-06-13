package ru.practicum.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static ParticipationRequestDto toParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                                      .id(request.getId())
                                      .created(request.getCreated())
                                      .requester(request.getRequester().getId())
                                      .event(request.getEvent().getId())
                                      .status(request.getStatus())
                                      .build();
    }
}
