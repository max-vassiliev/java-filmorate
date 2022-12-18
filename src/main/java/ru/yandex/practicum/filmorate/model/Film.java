package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
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


    public Film(Integer id, String name, int totalLikes) {
        this.id = id;
        this.name = name;
        this.totalLikes = totalLikes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return totalLikes == film.totalLikes
                && Objects.equals(id, film.id)
                && Objects.equals(name, film.name)
                && Objects.equals(description, film.description)
                && Objects.equals(releaseDate, film.releaseDate)
                && Objects.equals(mpa, film.mpa)
                && Objects.equals(duration, film.duration)
                && Objects.equals(genres, film.genres)
                && Objects.equals(likes, film.likes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, releaseDate, mpa, duration, totalLikes, genres, likes);
    }
}
