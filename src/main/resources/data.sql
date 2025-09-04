MERGE INTO GENRE (id, name) VALUES (1, 'Комедия');
MERGE INTO GENRE (id, name) VALUES (2, 'Драма');
MERGE INTO GENRE (id, name) VALUES (3, 'Мультфильм');
MERGE INTO GENRE (id, name) VALUES (4, 'Триллер');
MERGE INTO GENRE (id, name) VALUES (5, 'Документальный');
MERGE INTO GENRE (id, name) VALUES (6, 'Боевик');

MERGE INTO MPA_RATING (id, name, description) VALUES (1, 'G', 'Film is suitable for all ages and contains nothing in its themes, language, violence, or other elements that would offend parents of young children.');
MERGE INTO MPA_RATING (id, name, description) VALUES (2, 'PG', 'Film may contain some material that is not suitable for young children, and parents should consider whether the content is appropriate for their individual children.');
MERGE INTO MPA_RATING (id, name, description) VALUES (3, 'PG-13', 'Some material may be inappropriate for children under 13," is a warning that a film may contain content not suitable for pre-teens.');
MERGE INTO MPA_RATING (id, name, description) VALUES (4, 'R','Film contains adult material and children under 17 require an accompanying parent or adult guardian to be admitted. R-rated films may include elements such as strong language, intense or graphic violence, nudity, drug abuse, and sexually-oriented content.');
MERGE INTO MPA_RATING (id, name, description) VALUES (5, 'NC-17', 'Film contains adult content, such as excessive violence, sex, drug use, or aberrant behavior, that most parents would find too mature for children under 18.');

MERGE INTO film (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_RATING_ID)
    VALUES (1,
        'Fear and loathing in Las-Vegas',
        'I really cant say why I remembered this movie',
        '1998-05-02',
        118,
        4);

MERGE INTO FILM_GENRE (ID, FILM_ID, GENRE_ID) VALUES ( 1, 1, 6);
MERGE INTO FILM_GENRE (ID, FILM_ID, GENRE_ID) VALUES ( 2, 1, 4);

MERGE INTO "USER" (ID, EMAIL, LOGIN, NAME, BIRTHDATE)
VALUES (1,
        'ostap@email.cz',
        'ostap-login',
        'Ostap Ibragimovich',
        '2020-01-01');