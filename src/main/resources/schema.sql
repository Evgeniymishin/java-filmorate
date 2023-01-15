CREATE TABLE IF NOT EXISTS MPA (
    mpa_id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar
);

CREATE TABLE IF NOT EXISTS FILM (
    film_id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar,
    description varchar,
    release_date date,
    duration integer,
    mpa_id int REFERENCES Mpa(mpa_id)
);

CREATE TABLE IF NOT EXISTS GENRE (
    genre_id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar
);

CREATE TABLE IF NOT EXISTS FILMGENRE (
    film_id integer REFERENCES Film(film_id),
    genre_id integer REFERENCES Genre(genre_id),
    CONSTRAINT "filmgenre" PRIMARY KEY (film_id, genre_id),
    CONSTRAINT "filmgenre_film_id" FOREIGN KEY (film_id) REFERENCES film(film_id),
    CONSTRAINT "filmgenre_genre_id" FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
);

CREATE TABLE IF NOT EXISTS USERS (
    user_id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email varchar,
    login varchar,
    name varchar,
    birthday date
);

CREATE TABLE IF NOT EXISTS FILMLIKES (
    film_id integer REFERENCES Film(film_id),
    user_id integer REFERENCES Users(user_id),
    CONSTRAINT "films_likes" PRIMARY KEY (film_id, user_id),
    CONSTRAINT "films_likes_film_id" FOREIGN KEY (film_id) REFERENCES film(film_id),
    CONSTRAINT "films_likes_user_id" FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS USERFRIENDS (
    initial_user_id integer REFERENCES Users(user_id),
    second_user_id integer REFERENCES Users(user_id),
    CONSTRAINT "userfriends_pk" PRIMARY KEY (initial_user_id, second_user_id),
    CONSTRAINT "userfriends_user_id" FOREIGN KEY (initial_user_id) REFERENCES Users(user_id),
    CONSTRAINT "second_user_id" FOREIGN KEY (second_user_id) REFERENCES Users(user_id)
);

CREATE TABLE IF NOT EXISTS REVIEWS (
    id INTEGER AUTO_INCREMENT,
    content VARCHAR,
    is_positive BOOLEAN NOT NULL,
    user_id INTEGER,
    film_id INTEGER,
    CONSTRAINT "reviews" PRIMARY KEY (id),
    CONSTRAINT "reviews_user_id" FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT "reviews_film_id" FOREIGN KEY (film_id) REFERENCES FILM(film_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS REVIEWSRATING (
    id INTEGER AUTO_INCREMENT,
    review_id INTEGER,
    user_id INTEGER,
    likes BOOLEAN DEFAULT FALSE,
    dislikes BOOLEAN DEFAULT FALSE,
    CONSTRAINT "reviews_rating" PRIMARY KEY (id),
    CONSTRAINT "reviews_rating_user_id" FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT "reviews_rating_review_id" FOREIGN KEY (review_id) REFERENCES REVIEWS(id) ON DELETE CASCADE ON UPDATE CASCADE
);