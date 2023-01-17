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

CREATE TABLE IF NOT EXISTS DIRECTOR (
    director_id integer NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name varchar
);

CREATE TABLE IF NOT EXISTS FILMDIRECTOR (
    film_id integer,
    director_id integer,
    CONSTRAINT "film_director" PRIMARY KEY (film_id, director_id),
    CONSTRAINT "film_director_film_id" FOREIGN KEY (film_id) REFERENCES film(film_id),
    CONSTRAINT "film_director_director_id" FOREIGN KEY (director_id) REFERENCES director(director_id)
);