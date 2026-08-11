INSERT INTO types (id, name) VALUES
  (1, 'cat'), (2, 'dog'), (3, 'lizard'), (4, 'snake'), (5, 'bird'), (6, 'hamster');

INSERT INTO owners (id, first_name, last_name, address, city, telephone) VALUES
  (1, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023'),
  (2, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749'),
  (3, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763'),
  (4, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198'),
  (5, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765'),
  (6, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654'),
  (7, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387'),
  (8, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683'),
  (9, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435'),
  (10, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

INSERT INTO pets (id, name, birth_date, type_id, owner_id) VALUES
  (1, 'Leo', '2010-09-07', 1, 1),
  (2, 'Basil', '2012-08-06', 6, 2),
  (3, 'Rosy', '2011-04-17', 2, 3),
  (4, 'Jewel', '2010-03-07', 2, 3),
  (5, 'Iggy', '2010-11-30', 3, 4),
  (6, 'George', '2010-01-20', 4, 5),
  (7, 'Samantha', '2012-09-04', 1, 6),
  (8, 'Max', '2012-09-04', 1, 6),
  (9, 'Lucky', '2011-08-06', 5, 7),
  (10, 'Mulligan', '2007-02-24', 2, 8),
  (11, 'Freddy', '2010-03-09', 5, 9),
  (12, 'Lucky', '2010-06-24', 2, 10),
  (13, 'Sly', '2012-06-08', 1, 10);

-- Identity columns were seeded with explicit ids; move the sequences past them
ALTER TABLE types ALTER COLUMN id RESTART WITH 7;
ALTER TABLE owners ALTER COLUMN id RESTART WITH 11;
ALTER TABLE pets ALTER COLUMN id RESTART WITH 14;
