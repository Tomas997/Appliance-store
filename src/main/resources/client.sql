-- Client rows share the "users" id sequence with Employee/Deliverer (JOINED inheritance),
-- so ids are assigned explicitly here (1-13) to keep orders.sql's client_id references valid.
INSERT INTO users (id, name, email, password) VALUES
    (1, 'Mercury','mercury@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (2, 'Venus','venus@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (3, 'Earth','earth@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (4, 'Mars','IamGod@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (5, 'Jupiter','jupiter@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (6, 'Saturn','saturn@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (7, 'Uranus','uranus@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (8, 'Neptune','neptune@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (9, 'Pluto','pluto@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (10, 'Ceres','ceres@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (11, 'Eris','eris@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (12, 'Haumea','haumea@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (13, 'Makemake','makemake@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu');

INSERT INTO client (id, card) VALUES
    (1, '5326-1111'),
    (2, '5326-2222'),
    (3, '5326-3333'),
    (4, '5326-4444'),
    (5, '5326-5555'),
    (6, '5326-6666'),
    (7, '5326-7777'),
    (8, '5326-8888'),
    (9, '5326-9999'),
    (10, '5326-1010'),
    (11, '5326-1111'),
    (12, '5326-1212'),
    (13, '5326-1313');
