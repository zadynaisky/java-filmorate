merge into GENRE (id, name) values (1, 'Comedy');
merge into GENRE (id, name) values (2, 'Drama');
merge into GENRE (id, name) values (3, 'Cartoon');
merge into GENRE (id, name) values (4, 'Thriller');
merge into GENRE (id, name) values (5, 'Documentary');
merge into GENRE (id, name) values (6, 'Action');

merge into MPA_RATING (id, name, description) values (1, 'G', 'Film is suitable for all ages and contains nothing in its themes, language, violence, or other elements that would offend parents of young children.');
merge into MPA_RATING (id, name, description) values (2, 'PG', 'Film may contain some material that is not suitable for young children, and parents should consider whether the content is appropriate for their individual children.');
merge into MPA_RATING (id, name, description) values (3, 'PG-13', 'Some material may be inappropriate for children under 13," is a warning that a film may contain content not suitable for pre-teens.');
merge into MPA_RATING (id, name, description) values (4, 'R','Film contains adult material and children under 17 require an accompanying parent or adult guardian to be admitted. R-rated films may include elements such as strong language, intense or graphic violence, nudity, drug abuse, and sexually-oriented content.');
merge into MPA_RATING (id, name, description) values (5, 'NC-17', 'Film contains adult content, such as excessive violence, sex, drug use, or aberrant behavior, that most parents would find too mature for children under 18.');