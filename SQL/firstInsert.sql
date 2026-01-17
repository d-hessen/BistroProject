-- FIRST INSERT INTO DB
INSERT INTO restaurant_config (
  restaurant_id, open_time, close_time, reservation_duration_hours, max_late_minutes
) VALUES
(1, '12:00:00', '23:00:00', 2, 15);

INSERT INTO members (full_name, phone, email, password , card_code) VALUES
('Daniil Hessen', '0506866881', 'burgzing@gmail.com', 'pass123', 'CARD-001');

INSERT INTO staff (username, password, full_name, role) VALUES
('manager', 'pass123', 'Daniil Hessen', 'manager'),
('worker', 'pass123', 'Bob Smith', 'worker');

INSERT INTO tables (table_number, capacity, is_active) VALUES
(1, 2, 1),
(2, 4, 1),
(3, 4, 1),
(4, 6, 1),
(5, 8, 1);

INSERT INTO regular_hours (day_name, open_time, close_time) VALUES 
('Sunday', '08:00', '22:00'),
('Monday', '08:00', '22:00'),
('Tuesday', '08:00', '22:00'),
('Wednesday', '08:00', '22:00'),
('Thursday', '08:00', '22:00'),
('Friday', '08:00', '15:00'),
('Saturday', '18:00', '22:00');