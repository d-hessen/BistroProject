INSERT INTO restaurant_config (
  restaurant_id, open_time, close_time, reservation_duration_hours, max_late_minutes
) VALUES
(1, '12:00:00', '23:00:00', 2, 15);

INSERT INTO reservation (
  reservation_number, reservation_date, reservation_time, number_of_guests, verification_code,
  member_id, guest_full_name, guest_phone, email, status
) VALUES
('1','2026-01-06','23:38:00', 2, 'VER001', 1, NULL, NULL, NULL, 'approved');

INSERT INTO visits (
  table_id, reservation_number, waiting_id,
  start_time, end_time, is_active
) VALUES
(1, 1, NULL, '2026-01-07 18:05:00', '2026-01-07 19:30:00', 1);

INSERT INTO members (full_name, phone, email, password , card_code) VALUES
('John Doe', '0506866881', 'john@example.com', 'pass123', 'CARD-001');

INSERT INTO staff (username, password, full_name, role) VALUES
('manager', 'pass123', 'Daniil Hessen', 'manager'),
('worker', 'pass123', 'Bob Smith', 'worker');

INSERT INTO waiting_list (
  member_id, guest_full_name, guest_phone,email, number_of_guests, status
) VALUES
(NULL, 'Daniil', '1231231231','A@A', 2, 'waiting');

INSERT INTO bills (
  visit_id, member_id,
  total_amount, discount_amount, final_amount,
  is_paid
) VALUES
(1, NULL, 120.00, 12.00, 108.00, 1);

INSERT INTO tables (table_number, capacity, is_active) VALUES
(1, 2, 1),
(2, 4, 1),
(3, 4, 1),
(4, 6, 1),
(5, 8, 1);

INSERT INTO members (full_name, phone, email, password , card_code) VALUES
('John Doe', '0506866881', 'john@example.com', 'pass123', 'CARD-001'),
('Tom Halland', '555-1002', 'tom@example.com', 'pass123', 'CARD-002'),
('Will Smith', '555-1003', 'will@example.com', 'pass123', 'CARD-003'),
('Emma Stone', '555-1004', 'emma@example.com', 'pass123', 'CARD-004'),
('Donald Trump', '555-1005', 'donald@example.com', 'pass123', 'CARD-005');

INSERT INTO reservation (
  reservation_number, reservation_date, reservation_time, number_of_guests, verification_code,
  member_id, guest_full_name, guest_phone, email, status
) VALUES
('1','2025-01-10','18:00:00', 2, 'VER001', 1, NULL, NULL, NULL, 'approved'),
('2','2025-01-10','19:00:00', 4, 'VER002', 2, NULL, NULL, NULL, 'pending'),
('3','2025-01-11','20:00:00', 3, 'VER003', NULL, 'Alex Guest', '555-2001', 'alex@example.com', 'approved'),
('4','2025-01-12','17:30:00', 5, 'VER004', 3, NULL, NULL, NULL, 'seated'),
('5','2025-01-12','21:00:00', 2, 'VER005', NULL, 'Sarah Guest', '555-2002', NULL, 'cancelled');

INSERT INTO waiting_list (
  member_id, guest_full_name, guest_phone, number_of_guests, status
) VALUES
(4, NULL, NULL, 2, 'waiting'),
(NULL, 'Guest One', '555-3001', 3, 'waiting'),
(5, NULL, NULL, 4, 'notified'),
(NULL, 'Guest Two', '555-3002', 2, 'seated'),
(1, NULL, NULL, 6, 'cancelled');

INSERT INTO visits (
  table_id, reservation_number, waiting_id,
  start_time, end_time, is_active
) VALUES
(1, 1, NULL, '18:05:00', '19:30:00', 0),
(2, 4, NULL, '17:35:00', NULL, 1),
(3, NULL, 2, '18:20:00', '19:45:00', 0),
(4, NULL, 4, '20:00:00', NULL, 1),
(5, 2, NULL, '19:05:00', NULL, 1);

INSERT INTO bills (
  visit_id, member_id,
  total_amount, discount_amount, final_amount,
  is_paid
) VALUES
(1, 1, 120.00, 12.00, 108.00, 1),
(2, 3, 200.00, 20.00, 180.00, 0),
(3, NULL, 90.00, 0.00, 90.00, 1),
(4, NULL, 150.00, 0.00, 150.00, 0),
(5, 2, 75.00, 7.50, 67.50, 1);


