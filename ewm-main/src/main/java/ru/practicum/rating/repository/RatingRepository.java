package ru.practicum.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.rating.model.Rating;

public interface RatingRepository extends JpaRepository<Rating, Integer> {

    Rating findByUserIdAndEventId(Integer userId, Integer eventId);

    @Query("select count(r.id) " +
           "from Rating as r " +
           "where r.event.id = ?1 " +
           "and r.status = true ")
    int countRatingTrue(Integer eventId);

    @Query("select count(r.id) " +
            "from Rating as r " +
            "where r.event.id = ?1 " +
            "and r.status = false ")
    int countRatingFalse(Integer eventId);
}
