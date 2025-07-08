INSERT INTO person (id, firstname, lastname) VALUES (1, 'Bob', 'Andrews');
INSERT INTO person (id, firstname, lastname) VALUES (2, 'Justus', 'Jonas');
INSERT INTO person (id, firstname, lastname) VALUES (3, 'Peter', 'Shaw');
ALTER TABLE person ALTER COLUMN id RESTART WITH 4;

INSERT INTO book (id, title, author, publication_year) VALUES (1, 'Dune', 'Frank Herbert', 1965);
INSERT INTO book (id, title, author, publication_year) VALUES (2, 'The Hobbit', 'J.R.R. Tolkien', 1937);
INSERT INTO book (id, title, author, publication_year) VALUES (3, 'The Hunger Games', 'Suzanne Collins', 2008);
ALTER TABLE book ALTER COLUMN id RESTART WITH 4;
