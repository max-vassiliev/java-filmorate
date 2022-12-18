create table IF NOT EXISTS MPA
(
    MPA_ID   INTEGER auto_increment,
    MPA_NAME CHARACTER VARYING(10) not null,
    constraint "MPA_pk"
        primary key (MPA_ID)
);

create table IF NOT EXISTS FILMS
(
    FILM_ID      INTEGER auto_increment,
    NAME         CHARACTER VARYING(70) not null,
    DESCRIPTION  CHARACTER VARYING(500),
    RELEASE_DATE DATE,
    MPA          INTEGER,
    DURATION     INTEGER,
    LIKES        INTEGER,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint FILMS_MPA_MPA_ID_FK
        foreign key (MPA) references MPA
);

create table IF NOT EXISTS USERS
(
    USER_ID   INTEGER auto_increment,
    EMAIL     CHARACTER VARYING(50) not null,
    LOGIN     CHARACTER VARYING(30) not null,
    USER_NAME CHARACTER VARYING(30) not null,
    BIRTHDAY  DATE,
    constraint USERS_PK
        primary key (USER_ID)
);

create table IF NOT EXISTS GENRES
(
    GENRE_ID INTEGER auto_increment,
    GENRE    CHARACTER VARYING(15),
    constraint "GENRES_pk"
        primary key (GENRE_ID)
);

create table IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint FILM_GENRE_FILMS_FILM_ID_FK
        foreign key (FILM_ID) references FILMS,
    constraint FILM_GENRE_GENRES_GENRE_ID_FK
        foreign key (GENRE_ID) references GENRES
);

create table IF NOT EXISTS FILM_LIKE
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint "FILM_LIKE_pk"
        primary key (FILM_ID, USER_ID),
    constraint "FILMS_LIKES_FILMS_null_fk"
        foreign key (FILM_ID) references FILMS,
    constraint FILMS_LIKES_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS
);

create table IF NOT EXISTS FRIENDS
(
    USER_ID   INTEGER not null,
    FRIEND_ID INTEGER not null,
    constraint "FRIENDS_pk"
        primary key (USER_ID, FRIEND_ID),
    constraint FRIENDS_USERS_USER_ID_FK
        foreign key (USER_ID) references USERS,
    constraint FRIENDS_USERS_USER_ID_FK_2
        foreign key (FRIEND_ID) references USERS
);