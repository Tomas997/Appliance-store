-- Continues the shared "users" id sequence after client.sql (ids 1-13) -> employees get 14-25.
INSERT INTO users (id, name, email, password) VALUES
    (14, 'Phobos','phobos@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (15, 'Moon','phobos22@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (16, 'Deimos','deimos@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (17, 'Europa','europa@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (18, 'Ganymede','ganymede@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (19, 'Callisto','callisto@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (20, 'Io','io@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (21, 'Titan','titan@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (22, 'Rhea','rhea@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (23, 'Iapetus','iapetus@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (24, 'Dione','dione@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu'),
    (25, 'Tethys','tethys@gmail.com','$2a$10$kXWfqVO..gZJdTQCVyy8XemJcepiIPAZvuXxpn1C4gIESEsR4Uuxu');

INSERT INTO employee (id, department) VALUES
    (14, 'salle'),
    (15, 'salle'),
    (16, 'security'),
    (17, 'security'),
    (18, 'salle'),
    (19, 'salle'),
    (20, 'security'),
    (21, 'security'),
    (22, 'warehouse'),
    (23, 'warehouse'),
    (24, 'salle'),
    (25, 'security');
