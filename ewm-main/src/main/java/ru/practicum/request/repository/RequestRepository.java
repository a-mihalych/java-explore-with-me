package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Integer> {

    List<Request> findByEventIdAndStatus(Integer eventId, String status);

    List<Request> findByEventIdAndRequesterId(Integer eventId, Integer requesterId);

    Optional<Request> findByIdAndRequesterId(Integer requestId, Integer userId);

    List<Request> findByRequesterId(Integer userId);

    List<Request> findAllByIdIn(List<Integer> requestIds);

    List<Request> findByEventId(Integer eventId);

    @Query("select count(id) " +
           "from Request " +
           "where event = ?1 " +
           "and status = 'CONFIRMED'")
    Integer countRequestConfirmed(Integer eventId);
}
