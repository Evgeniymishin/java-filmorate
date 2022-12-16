# java-filmorate

![Filmogram](https://user-images.githubusercontent.com/32869331/208204808-863beaf5-0a3b-46e6-95a2-a35ef9fd2c54.png)

## Запросы (примерные)

Добавление в друзья:

```
INSERT INTO UserFriends (initialUserId, secondUserId, status)
VALUES (initialUserId, secondUserId, status)
```

Удаление из друзей 
```
UPDATE UserFriends
SET status = newStatus
WHERE (initialUserId = initialUserId AND secondUserId = secondUserId) OR (initialUserId = secondUserId AND secondUserId = initialUserId)
```

Вывести список друзей пользователя
```
SELECT *
FROM User u
INNER JOIN UserFriends uf ON (u.id = uf.initialUserId OR u.id = uf.secondUserId AND status = 'friend')
WHERE u.id = id
```

Вывести список общих друзей
```

```


Лайк фильма
```
INSERT INTO FilmLikes (filmId, userId)
VALUES (filmId, userId)
```


Дизлайк фильма
```
DELETE FROM FilmLikes
WHERE filmId = filmId, userId = userId
```

Вывести популярные фильмов
```

```
