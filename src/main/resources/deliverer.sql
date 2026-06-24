-- Continues the shared "users" id sequence after employee.sql (ids 14-25) -> deliverers get 26-37.
INSERT INTO users (id, name, email, password) VALUES
    (26, 'Sirius','sirius@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (27, 'Vega','vega@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (28, 'Altair','altair@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (29, 'Polaris','polaris@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (30, 'Rigel','rigel@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (31, 'Betelgeuse','betelgeuse@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (32, 'Capella','capella@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (33, 'Antares','antares@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (34, 'Spica','spica@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (35, 'Procyon','procyon@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (36, 'Pollux','pollux@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (37, 'Castor','castor@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu');

INSERT INTO deliverer (id) VALUES
    (26), (27), (28), (29), (30), (31), (32), (33), (34), (35), (36), (37);
