-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: bistro
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `visit_id` int NOT NULL,
  `member_id` int DEFAULT NULL,
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `discount_amount` decimal(10,2) DEFAULT '0.00',
  `final_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `payment_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_paid` tinyint DEFAULT '0',
  PRIMARY KEY (`bill_id`),
  KEY `fk_bills_session` (`visit_id`),
  KEY `fk_bills_sub` (`member_id`),
  CONSTRAINT `fk_bills_session` FOREIGN KEY (`visit_id`) REFERENCES `visits` (`visit_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bills_sub` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bills`
--

LOCK TABLES `bills` WRITE;
/*!40000 ALTER TABLE `bills` DISABLE KEYS */;
INSERT INTO `bills` VALUES (1,1,1,120.00,12.00,108.00,'2025-12-19 16:36:45',1),(2,2,3,200.00,20.00,180.00,'2025-12-19 16:36:45',0),(3,3,NULL,90.00,0.00,90.00,'2025-12-19 16:36:45',1),(4,4,NULL,150.00,0.00,150.00,'2025-12-19 16:36:45',0),(5,5,2,75.00,7.50,67.50,'2025-12-19 16:36:45',1);
/*!40000 ALTER TABLE `bills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `members`
--

DROP TABLE IF EXISTS `members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `members` (
  `member_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `card_code` varchar(50) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `phone` (`phone`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `card_code` (`card_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `members`
--

LOCK TABLES `members` WRITE;
/*!40000 ALTER TABLE `members` DISABLE KEYS */;
INSERT INTO `members` VALUES (1,'John Doe','555-1001','john@example.com','pass123','CARD-001','2025-12-19 14:36:45'),(2,'Tom Halland','555-1002','tom@example.com','pass123','CARD-002','2025-12-19 14:36:45'),(3,'Will Smith','555-1003','will@example.com','pass123','CARD-003','2025-12-19 14:36:45'),(4,'Emma Stone','555-1004','emma@example.com','pass123','CARD-004','2025-12-19 14:36:45'),(5,'Donald Trump','555-1005','donald@example.com','pass123','CARD-005','2025-12-19 14:36:45');
/*!40000 ALTER TABLE `members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `reservation_number` int NOT NULL AUTO_INCREMENT,
  `reservation_date` datetime NOT NULL,
  `number_of_guests` int NOT NULL,
  `verification_code` varchar(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `member_id` int DEFAULT NULL,
  `guest_full_name` varchar(100) DEFAULT NULL,
  `guest_phone` varchar(20) DEFAULT NULL,
  `status` enum('pending','approved','seated','cancelled','no_show') DEFAULT 'pending',
  PRIMARY KEY (`reservation_number`),
  KEY `fk_res_subscribers` (`member_id`),
  CONSTRAINT `fk_res_subscribers` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
INSERT INTO `reservation` VALUES (1,'2025-01-10 18:00:00',2,'VER001','2025-12-19 14:36:45',1,NULL,NULL,'approved'),(2,'2025-01-10 19:00:00',4,'VER002','2025-12-19 14:36:45',2,NULL,NULL,'pending'),(3,'2025-01-11 20:00:00',3,'VER003','2025-12-19 14:36:45',NULL,'Alex Guest','555-2001','approved'),(4,'2025-01-12 17:30:00',5,'VER004','2025-12-19 14:36:45',3,NULL,NULL,'seated'),(5,'2025-01-12 21:00:00',2,'VER005','2025-12-19 14:36:45',NULL,'Sarah Guest','555-2002','cancelled');
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `restaurant_config`
--

DROP TABLE IF EXISTS `restaurant_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant_config` (
  `restaurant_id` int NOT NULL DEFAULT '1',
  `open_time` time NOT NULL DEFAULT '12:00:00',
  `close_timet` time NOT NULL DEFAULT '23:00:00',
  `reservation_duration_hours` int NOT NULL DEFAULT '2',
  `max_late_minutes` int NOT NULL DEFAULT '15',
  PRIMARY KEY (`restaurant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant_config`
--

LOCK TABLES `restaurant_config` WRITE;
/*!40000 ALTER TABLE `restaurant_config` DISABLE KEYS */;
INSERT INTO `restaurant_config` VALUES (1,'12:00:00','23:00:00',2,15);
/*!40000 ALTER TABLE `restaurant_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `staff_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `role` enum('worker','manager') NOT NULL,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (1,'manager1','pass123','Daniil Hessen','manager'),(2,'manager2','pass123','Tedy Haddad','manager'),(3,'manager3','pass123','Ori Kalmanovich','manager'),(4,'manager4','pass123','Tomer Levy','manager'),(5,'worker1','pass123','Bob Smith','worker'),(6,'worker2','pass123','Charlie Brown','worker'),(7,'worker3','pass123','Diana Ross','worker'),(8,'worker4','pass123','Ethan Clark','worker');
/*!40000 ALTER TABLE `staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tables`
--

DROP TABLE IF EXISTS `tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tables` (
  `table_id` int NOT NULL AUTO_INCREMENT,
  `table_number` int NOT NULL,
  `capacity` int NOT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`table_id`),
  UNIQUE KEY `table_number` (`table_number`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tables`
--

LOCK TABLES `tables` WRITE;
/*!40000 ALTER TABLE `tables` DISABLE KEYS */;
INSERT INTO `tables` VALUES (1,1,2,1),(2,2,4,1),(3,3,4,1),(4,4,6,1),(5,5,8,1);
/*!40000 ALTER TABLE `tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `visits`
--

DROP TABLE IF EXISTS `visits`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visits` (
  `visit_id` int NOT NULL AUTO_INCREMENT,
  `table_id` int NOT NULL,
  `reservation_number` int DEFAULT NULL,
  `waiting_id` int DEFAULT NULL,
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL,
  `is_active` tinyint DEFAULT '1',
  PRIMARY KEY (`visit_id`),
  KEY `fk_session_table` (`table_id`),
  KEY `fk_session_res` (`reservation_number`),
  KEY `fk_session_wait` (`waiting_id`),
  CONSTRAINT `fk_session_res` FOREIGN KEY (`reservation_number`) REFERENCES `reservation` (`reservation_number`),
  CONSTRAINT `fk_session_table` FOREIGN KEY (`table_id`) REFERENCES `tables` (`table_id`),
  CONSTRAINT `fk_session_wait` FOREIGN KEY (`waiting_id`) REFERENCES `waiting_list` (`waiting_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visits`
--

LOCK TABLES `visits` WRITE;
/*!40000 ALTER TABLE `visits` DISABLE KEYS */;
INSERT INTO `visits` VALUES (1,1,1,NULL,'2025-01-10 18:05:00','2025-01-10 19:30:00',0),(2,2,4,NULL,'2025-01-12 17:35:00',NULL,1),(3,3,NULL,2,'2025-01-10 18:20:00','2025-01-10 19:45:00',0),(4,4,NULL,4,'2025-01-10 20:00:00',NULL,1),(5,5,2,NULL,'2025-01-10 19:05:00',NULL,1);
/*!40000 ALTER TABLE `visits` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `waiting_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int DEFAULT NULL,
  `guest_full_name` varchar(100) DEFAULT NULL,
  `guest_phone` varchar(20) DEFAULT NULL,
  `number_of_guests` int NOT NULL,
  `entered_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `notified_at` datetime DEFAULT NULL,
  `status` enum('waiting','notified','seated','cancelled') DEFAULT 'waiting',
  PRIMARY KEY (`waiting_id`),
  KEY `fk_wait_members` (`member_id`),
  CONSTRAINT `fk_wait_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
INSERT INTO `waiting_list` VALUES (1,4,NULL,NULL,2,'2025-12-19 16:36:45',NULL,'waiting'),(2,NULL,'Guest One','555-3001',3,'2025-12-19 16:36:45',NULL,'waiting'),(3,5,NULL,NULL,4,'2025-12-19 16:36:45',NULL,'notified'),(4,NULL,'Guest Two','555-3002',2,'2025-12-19 16:36:45',NULL,'seated'),(5,1,NULL,NULL,6,'2025-12-19 16:36:45',NULL,'cancelled');
/*!40000 ALTER TABLE `waiting_list` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-19 16:39:59
