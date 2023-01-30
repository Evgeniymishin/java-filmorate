package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewRating;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewDbStorageImpl implements ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Review> create(Review review) {
        String check = "select id from REVIEWS where film_id = ? and user_id = ?";
        List<Integer> list = jdbcTemplate.query(check, (r, i) -> r.getInt("id"), review.getFilmId(), review.getUserId());
        if (list.isEmpty()) {
            SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("REVIEWS")
                    .usingGeneratedKeyColumns("id");
            Integer id = insert.executeAndReturnKey(review.toMap()).intValue();
            return getById(id);
        } else {
            review.setReviewId(list.get(0));
            return update(review);
        }
    }

    @Override
    public Optional<Review> getById(Integer id) {
        String query = "select a.*, ((select count(*) from REVIEWSRATING where review_id = ? and likes = true) - " +
                "(select count(*) from REVIEWSRATING where review_id = ? and dislikes = true))::integer as useful " +
                "from REVIEWS a where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, this::mapRowToReview, id, id, id));
        } catch (DataAccessException e) {
            log.warn("Отзыв с идентификатором {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Integer id) {
        String query = "delete from REVIEWS where id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public Optional<Review> update(Review rowData) {
        String query = "update REVIEWS set content = ?, is_positive = ? where id = ?";
        int update = jdbcTemplate.update(query
                , rowData.getContent()
                , rowData.getIsPositive()
                , rowData.getReviewId());
        if (update > 0) {
            return getById(rowData.getReviewId());
        }
        return Optional.empty();
    }

    @Override
    public List<Review> getFilmReview(Integer filmId, Integer limit) {
        String query = "select a.*, ((select count(*) from REVIEWSRATING where review_id = a.id and likes = true) - " +
                "(select count(*) from REVIEWSRATING where review_id = a.id and dislikes = true))::integer as useful " +
                "from REVIEWS a where film_id = (case when ? is not null then ? else film_id end) " +
                "order by useful desc  limit ?";
        return jdbcTemplate.query(query, this::mapRowToReview, filmId, filmId, limit);
    }

    @Override
    public Optional<ReviewRating> manageLikeDislike(Integer id, Integer userId, Boolean isLike, Boolean value) {
        String likesOrDislikes;
        if (Boolean.TRUE.equals(isLike)) {
            likesOrDislikes = "likes";
        } else {
            likesOrDislikes = "dislikes";
        }
        String check = "select id from REVIEWSRATING where review_id = ? and user_id = ?";
        List<Integer> list = jdbcTemplate.query(check, (r, i) -> r.getInt("id"), id, userId);
        if (list.isEmpty()) {
            String query = "insert into REVIEWSRATING (review_id, user_id, " + likesOrDislikes + ") values (?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(query, new String[]{"id"});
                stmt.setInt(1, id);
                stmt.setInt(2, userId);
                stmt.setBoolean(3, value);
                return stmt;
            }, keyHolder);
            return getRatingById(keyHolder.getKeyAs(Integer.class));
        } else {
            String query = "update REVIEWSRATING set " + likesOrDislikes + " = " + value + " where id = ?";
            jdbcTemplate.update(query, list.get(0));
            return getRatingById(list.get(0));
        }
    }

    @Override
    public Optional<ReviewRating> getRatingById(Integer id) {
        String query = "select * from REVIEWSRATING where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, this::mapRowToReviewRating, id));
        } catch (DataAccessException e) {
            log.warn("Рейтинг с идентификатором {} не найден", id);
            return Optional.empty();
        }
    }

    private ReviewRating mapRowToReviewRating(ResultSet resultSet, int i) throws SQLException {
        return ReviewRating.builder()
                .id(resultSet.getInt("id"))
                .reviewId(resultSet.getInt("review_id"))
                .userId(resultSet.getInt("user_id"))
                .like(resultSet.getBoolean("likes"))
                .dislike(resultSet.getBoolean("dislikes"))
                .build();
    }

    private Review mapRowToReview(ResultSet resultSet, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(resultSet.getInt("id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .userId(resultSet.getInt("user_id"))
                .filmId(resultSet.getInt("film_id"))
                .useful(resultSet.getInt("useful"))
                .build();
    }
}
