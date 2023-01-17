package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
@Builder
@AllArgsConstructor
public class Film {
    private Integer id;
    @NotBlank
    @NotNull
    private String name;
    @Size(max=200)
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    @Positive
    private Integer duration;
    private Mpa mpa;
    private LinkedHashSet<Genre> genres;
    private LinkedHashSet<Director> directors;
}
