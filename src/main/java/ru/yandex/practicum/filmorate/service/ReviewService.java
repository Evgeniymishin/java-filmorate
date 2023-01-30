package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedDbStorage;
import ru.yandex.practicum.filmorate.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewRating;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewService {
    private final ReviewDbStorage storage;
    private final UserService userService;
    private final FilmService filmService;
    private final FeedDbStorage feedStorage;

    public Review addReview(Review rowData) {
        userService.getById(rowData.getUserId());
        filmService.getById(rowData.getFilmId());
        return storage.create(rowData).map(review -> {
                    log.info("Добавлен новый отзыв: {}", review.toString());
                    feedStorage.addFeed(review.getUserId(), review.getReviewId(), Operation.ADD, EventType.REVIEW);
                    return review;
                })
                .orElseThrow(() -> {
                    log.warn("Ошибка при добавлении отзыва: {}", rowData);
                    throw new NotFoundException("Ошибка при добавлении отзыва");
                });
    }

    public Review getReview(Integer id) {
        return storage.getById(id).orElseThrow(() -> {
            log.warn("Отзыв с идентификатором id = {} не найден", id);
            throw new NotFoundException("Отзыв не найден");
        });
    }

    public void deleteReview(Integer id) {
        Review review = getReview(id);
        storage.deleteById(id);
        feedStorage.addFeed(review.getUserId(), id, Operation.REMOVE, EventType.REVIEW);
        log.info("Удален отзыв: {}", id);
    }

    public Review updateReview(Review rowData) {
        return storage.update(rowData).map(review -> {
                    log.info("Обновлен отзыв: {}", review.toString());
                    feedStorage.addFeed(review.getUserId(), review.getReviewId(), Operation.UPDATE, EventType.REVIEW);
                    return review;
                })
                .orElseThrow(() -> {
                    log.warn("Ошибка при обновлении отзыва: {}", rowData);
                    throw new NotFoundException("Отзыв не найден");
                });
    }

    public List<Review> getFilmReview(Integer filmId, Integer count) {
        return storage.getFilmReview(filmId, count);
    }

    public ReviewRating setLikesDislikes(Integer id, Integer userId, Boolean isLike, Boolean value) {
        return storage.manageLikeDislike(id, userId, isLike, value).map(reviewRating -> {
                    log.info("Установлен рейтинг отзыва: {}", reviewRating.toString());
                    return reviewRating;
                })
                .orElseThrow(() -> {
                    log.warn("Ошибка при установке рейтинга: отзыв - {}, пользователь - {}", id, userId);
                    throw new NotFoundException("Ошибка при установке отзыву рейтинга");
                });
    }
}
