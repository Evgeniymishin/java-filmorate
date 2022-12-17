package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;
    @NotBlank
    @NotNull
    private String name;
    @Size(max=200)
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    @Positive
    private int duration;
    Set<Integer> likes = new HashSet<>();

    public void like(Integer userId) {
        likes.add(userId);
    }

    public void disLike(Integer userId) {
        likes.remove(userId);
    }

    public Integer getLikesCount() {
        return likes.size();
    }

    public Set<Integer> getLikes() {
        return likes;
    }
}
