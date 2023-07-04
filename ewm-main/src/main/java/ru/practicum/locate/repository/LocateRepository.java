package ru.practicum.locate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.locate.model.Locate;

public interface LocateRepository extends JpaRepository<Locate, Integer> {

}
