package ru.practicum.locate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.locate.dto.Location;
import ru.practicum.locate.model.Locate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocateMapper {

    public static Locate toLocate(Location location) {
        return Locate.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public static Location toLocation(Locate locate) {
        return Location.builder()
                .lat(locate.getLat())
                .lon(locate.getLon())
                .build();
    }
}
