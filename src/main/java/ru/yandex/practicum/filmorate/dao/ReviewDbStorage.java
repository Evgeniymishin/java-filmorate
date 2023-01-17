package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewRating;

import java.util.List;
import java.util.Optional;

public interface ReviewDbStorage {
    Optional<Review> create(Review review);

    Optional<Review> getById(Integer id);

    void deleteById(Integer id);

    Optional<Review> update(Review rowData);

    List<Review> getFilmReview(Integer filmId, Integer limit);

    Optional<ReviewRating> manageLikeDislike(Integer id, Integer userId, Boolean isLike, Boolean value);

    Optional<ReviewRating> getRatingById(Integer id);
}
