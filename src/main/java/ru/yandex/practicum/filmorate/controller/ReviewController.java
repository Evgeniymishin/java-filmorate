package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewRating;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(value = "/reviews")
public class ReviewController {
    private final ReviewService service;

    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody @Valid @NotNull Review rowData) {
        return new ResponseEntity<>(service.addReview(rowData), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReview(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(service.getReview(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReview(@PathVariable("id") Integer id) {
        service.deleteReview(id);
    }

    @PutMapping()
    public ResponseEntity<Review> updateReview(@RequestBody @Valid @NotNull Review rowData) {
        return new ResponseEntity<>(service.updateReview(rowData), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Review>> getFilmReviews(@RequestParam(name = "filmId", required = false) Integer filmId
            , @RequestParam(name = "count", defaultValue = "10") @Positive Integer count) {
        return new ResponseEntity<>(service.getFilmReview(filmId, count), HttpStatus.OK);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<ReviewRating> likeReview(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        return new ResponseEntity<>(service.setLikesDislikes(id, userId, true, true), HttpStatus.OK);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<ReviewRating> dislikeReview(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        return new ResponseEntity<>(service.setLikesDislikes(id, userId, false, true), HttpStatus.OK);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReviewsLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        service.setLikesDislikes(id, userId, true, false);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReviewsDislike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        service.setLikesDislikes(id, userId, false, false);
    }

}
