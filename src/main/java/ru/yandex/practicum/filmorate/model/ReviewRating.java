package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ReviewRating {
    private Integer id;
    private Integer reviewId;
    private Integer userId;
    private Boolean like;
    private Boolean dislike;

}
