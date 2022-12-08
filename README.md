# java-filmorate


## Описание
Проект приложения **`Filmorate`** — базы данных фильмов. Пользователи могут отмечать свои любимые фильмы, находить любимые фильмы других пользователей и смотреть рейтинги популярных фильмов.

## Структура данных
Приложение хранит данные о фильмах (`film`) и пользователях (`user`), а также взаимосвязи между ними: с кем пользователь дружит (`friends`) и какие фильмы ему нравятся (`film_likes`). Для фильмов предусмотрена возможность присваивать несколько жанров (`genre`).

![ER Diagram](/java-filmorate-erd.png)
[ER-диаграмма базы данных Filmorate](https://app.quickdatabasediagrams.com/#/d/KepfNg) 

### Примеры SQL-запросов

#### Получить все фильмы
```sql
SELECT * FROM film;
```

#### Получить всех пользователей
```sql
SELECT * FROM user;
```

#### Получить всех друзей пользователя

```sql
-- получаем друзей пользователя 1
SELECT request_from
FROM friends
WHERE request_to = 1
AND status = 'confirmed'
UNION
SELECT request_to
FROM friends
WHERE request_from = 1
AND status = 'confirmed';
```

#### Получить список общих друзей
```sql
-- получаем общих друзей у пользователей 1 и 2
SELECT *
FROM (
    SELECT request_from 
    FROM friends
    WHERE request_to = 1
    AND status = 'confirmed'
    UNION
    SELECT request_to
    FROM friends
    WHERE request_from = 1
    AND status = 'confirmed'
) INNER JOIN (
    SELECT request_from
    FROM friends
    WHERE request_to = 2
    AND status = 'confirmed'
    UNION
    SELECT request_to
    FROM friends
    WHERE request_from = 2
    AND status = 'confirmed'
);
```

#### Получить любимые фильмы пользователя
```sql
-- получаем любимые фильмы пользователя 1
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.mpa_rating,
       f.duration
FROM film_likes AS fl
WHERE user_id = 1
INNER JOIN film AS f ON fl.film_id = f.film_id;
```

#### Получить общий список любимых фильмов с другим пользователем
```sql
-- получаем любимые фильмы пользователей 1 и 2
SELECT f.film_id, 
       f.name,
       f.description,
       f.release_date,
       f.mpa_rating,
       f.duration
FROM film AS f
WHERE film_id IN (SELECT film_id
                  FROM film_likes
                  WHERE user_id = 1
                  INNER JOIN
                  SELECT film_id
                  FROM film_likes
                  WHERE user_id = 2);
```

#### Получить все фильмы одного жанра
```sql
-- получаем все фильмы жанра 'Комедия'
SELECT f.film_id, 
       f.name,
       f.description,
       f.release_date,
       f.mpa_rating,
       f.duration
FROM film AS f
WHERE film_id IN (SELECT film_id
                  FROM film_genre AS fg
                  LEFT JOIN genre AS g ON fg.genre_id=g.genre_id
                  HAVING g.genre = 'Комедия');
```

#### Получить количество лайков у одного фильма 
```sql
-- считаем лайки у фильма 1
SELECT COUNT(film_id),
FROM film_likes
WHERE film_id = 1;
```

#### Получить топ-10 самых популярных фильмов
```sql
SELECT f.film_id, 
       f.name,
       f.description,
       f.release_date,
       f.mpa_rating,
       f.duration
FROM film AS f
WHERE film_id IN (SELECT film_id
                  FROM film_likes
                  GROUP BY film_id
                  ORDER BY COUNT(film_id)
                  LIMIT 10);
```
