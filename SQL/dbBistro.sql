-- Creating schema
CREATE SCHEMA IF NOT EXISTS `bistro` DEFAULT CHARACTER SET utf8mb4;
USE `bistro`;

-- MANAGEMENT SIDE (STAFF SIDE)
-- Restaurant configuration
CREATE TABLE IF NOT EXISTS `restaurant_config` (
  `restaurant_id` INT NOT NULL DEFAULT 1,
  `open_time` TIME NOT NULL DEFAULT '12:00:00',
  `close_timet` TIME NOT NULL DEFAULT '23:00:00',
  `reservation_duration_hours` INT NOT NULL DEFAULT 2, 
  `max_late_minutes` INT NOT NULL DEFAULT 15, 
  PRIMARY KEY (`restaurant_id`)
);

-- Staff(Including manager and regular workers)
CREATE TABLE IF NOT EXISTS `staff` (
  `staff_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(100) NOT NULL,
  `role` ENUM('worker', 'manager') NOT NULL,
  PRIMARY KEY (`staff_id`)
);

-- Tables
CREATE TABLE IF NOT EXISTS `tables` (
  `table_id` INT NOT NULL AUTO_INCREMENT, -- used for tracking all created tables
  `table_number` INT NOT NULL UNIQUE, -- table number st restaurant
  `capacity` INT NOT NULL, 
  `is_active` TINYINT NOT NULL DEFAULT 1, -- In case table not in use put 0
  PRIMARY KEY (`table_id`)
);


-- Clients side (Eaters)
-- Bistro Members
CREATE TABLE IF NOT EXISTS `members` (
  `member_id` INT NOT NULL AUTO_INCREMENT,
  `full_name` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(20)  NOT NULL UNIQUE,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL, 
  `card_code` VARCHAR(50) NOT NULL UNIQUE, -- QR Code/Member card
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- For tracking members reports
  PRIMARY KEY (`member_id`)
);

-- Reservations (include details of guests)
CREATE TABLE IF NOT EXISTS `reservation` (
  `reservation_number` INT NOT NULL AUTO_INCREMENT,
  `reservation_date` DATETIME NOT NULL, -- Date of reservation
  `number_of_guests` INT NOT NULL, -- Number of guests in reservation
  `verification_code` VARCHAR(20) NOT NULL, -- Code to start visit 
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `member_id` INT NULL, -- NULL if it's guest
  `guest_full_name` VARCHAR(100) NULL, -- Fill if there's no member_id
  `guest_phone` VARCHAR(20) NULL, -- Fill if there's no member_id
  `status` ENUM('pending', 'approved', 'seated', 'cancelled', 'no_show') DEFAULT 'pending',
  PRIMARY KEY (`reservation_number`),
  CONSTRAINT `fk_res_subscribers`
    FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`)
    ON DELETE SET NULL
);

-- Waiting List
CREATE TABLE IF NOT EXISTS `waiting_list` (
  `waiting_id` INT NOT NULL AUTO_INCREMENT, -- Applies to guest when enters
  `member_id` INT NULL,
  `guest_full_name` VARCHAR(100) NULL,
  `guest_phone` VARCHAR(20) NULL,
  `number_of_guests` INT NOT NULL,
  `entered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Time of entering waiting list
  `notified_at` DATETIME NULL, -- When was notified about free place
  `status` ENUM('waiting', 'notified', 'seated', 'cancelled') DEFAULT 'waiting',
  PRIMARY KEY (`waiting_id`),
  CONSTRAINT `fk_wait_members`
    FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`)
    ON DELETE SET NULL
);

-- Actual visit
CREATE TABLE IF NOT EXISTS `visits` (
  `visit_id` INT NOT NULL AUTO_INCREMENT,
  `table_id` INT NOT NULL,
  -- Has guest booked or joined from waiting list (for reports)
  `reservation_number` INT NULL, 
  `waiting_id` INT NULL,
  
  `start_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- when sitted
  `end_time` DATETIME NULL, -- actual end time
  
  -- Active (1) / Finished (0)
  `is_active` TINYINT DEFAULT 1,
  
  PRIMARY KEY (`visit_id`),
  CONSTRAINT `fk_session_table`
    FOREIGN KEY (`table_id`) REFERENCES `tables` (`table_id`),
  CONSTRAINT `fk_session_res`
    FOREIGN KEY (`reservation_number`) REFERENCES `reservation` (`reservation_number`),
  CONSTRAINT `fk_session_wait`
    FOREIGN KEY (`waiting_id`) REFERENCES `waiting_list` (`waiting_id`)
);

-- Bills
CREATE TABLE IF NOT EXISTS `bills` (
  `bill_id` INT NOT NULL AUTO_INCREMENT,
  `visit_id` INT NOT NULL,
  `member_id` INT NULL, -- To know if we need to apply 10% discount
  
  `total_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Entered by staff
  `discount_amount` DECIMAL(10, 2) DEFAULT 0.00, -- Set 10% if member_id != null
  `final_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- To pay
  
  `payment_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `is_paid` TINYINT DEFAULT 0,
  
  PRIMARY KEY (`bill_id`),
  CONSTRAINT `fk_bills_session`
    FOREIGN KEY (`visit_id`) REFERENCES `visits` (`visit_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_bills_sub`
    FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`)
);