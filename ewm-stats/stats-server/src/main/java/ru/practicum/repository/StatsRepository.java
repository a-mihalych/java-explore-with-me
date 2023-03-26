package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Integer> {

    @Query("select new ru.practicum.model.ViewStats(e.app, e.uri, count(distinct e.ip)) " +
           "from EndpointHit as e " +
           "where e.timestamp >= ?1 " +
           "and e.timestamp <= ?2 " +
           "and e.uri in ?3 " +
           "group by e.ip, e.uri " +
           "order by count(distinct e.ip) desc")
    List<ViewStats> StatsHitUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.model.ViewStats(e.app, e.uri, count(e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp >= ?1 " +
            "and e.timestamp <= ?2 " +
            "and e.uri in ?3 " +
            "group by e.ip, e.uri " +
            "order by count(e.ip) desc")
    List<ViewStats> StatsHitNotUnique(LocalDateTime start, LocalDateTime end, List<String> uris);
}
