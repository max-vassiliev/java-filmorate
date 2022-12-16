package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class Film {
    private Integer id;

    @NotBlank(message = "Укажите название фильма")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    private LocalDate releaseDate;

    private Mpa mpa;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    private int totalLikes;

    private Set<Genre> genres = new HashSet<>();

    private Set<Integer> likes = new HashSet<>();

    public Film(Integer id, String name, String description, LocalDate releaseDate,
                int duration, int totalLikes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.totalLikes = totalLikes;
    }
}
