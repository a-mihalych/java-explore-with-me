package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    List<Request> findByEventIdAndRequesterId(Integer eventId, Integer requesterId);

    List<Request> findByRequesterId(Integer userId);

    List<Request> findAllByIdIn(List<Integer> requestIds);

    List<Request> findByEventId(Integer eventId);

    @Query("select count(id) " +
           "from Request " +
           "where event.id = ?1 " +
           "and status = 'CONFIRMED'")
    Integer countRequestConfirmed(Integer eventId);
}
