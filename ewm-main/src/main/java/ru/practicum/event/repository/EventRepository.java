package ru.practicum.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findAllByInitiatorId(Integer userId, Pageable pageable);

    @Query("select e " +
            "from Event as e " +
            "where (?1 is null or e.initiator.id in ?1) " +
            "and (?2 is  null or e.eventState in ?2) " +
            "and (?3 is  null or e.category.id in ?3) " +
            "and (?4 is  null or e.eventDate >= ?4) " +
            "and (?5 is  null or e.eventDate <= ?5)")
    List<Event> eventsAdmin(List<Integer> users, List<String> states, List<Integer> categories,
                            LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("select e " +
            "from Event as e " +
            "where (?1 is null " +
                   "or lower(e.annotation) like concat('%', ?1, '%') " +
                   "or lower(e.description) like concat('%', ?1, '%')) " +
            "and (?2 is  null or e.category.id in ?2) " +
            "and (?3 is  null or e.paid = ?3) " +
            "and (?4 is  null or e.eventDate >= ?4) " +
            "and (?5 is  null or e.eventDate <= ?5) " +
            "and (e.eventState = 'PUBLISHED') " +
            "order by e.eventDate")
    List<Event> events(String text, List<Integer> categories, Boolean paid,
                       LocalDateTime start, LocalDateTime end, PageRequest of);
}
