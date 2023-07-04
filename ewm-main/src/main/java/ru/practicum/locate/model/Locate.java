package ru.practicum.locate.model;

import lombok.*;

import javax.persistence.*;

@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "locations")
public class Locate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "locate_id")
    private Integer id;
    private Float lat; // Широта
    private Float lon; // Долгота
}
