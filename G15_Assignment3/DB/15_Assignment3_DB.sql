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
  `payment_time` timestamp NULL DEFAULT NULL,
  `is_paid` tinyint DEFAULT '0',
  PRIMARY KEY (`bill_id`),
  UNIQUE KEY `visit_id` (`visit_id`),
  KEY `fk_bills_sub` (`member_id`),
  CONSTRAINT `fk_bills_session` FOREIGN KEY (`visit_id`) REFERENCES `visits` (`visit_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_bills_sub` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bills`
--

LOCK TABLES `bills` WRITE;
/*!40000 ALTER TABLE `bills` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `members`
--

LOCK TABLES `members` WRITE;
/*!40000 ALTER TABLE `members` DISABLE KEYS */;
INSERT INTO `members` VALUES (1,'Daniil Hessen','0506866881','burgzing@gmail.com','pass123','CARD-001','2026-01-18 00:35:56');
/*!40000 ALTER TABLE `members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `regular_hours`
--

DROP TABLE IF EXISTS `regular_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `regular_hours` (
  `day_name` varchar(20) NOT NULL,
  `open_time` varchar(5) DEFAULT NULL,
  `close_time` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`day_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `regular_hours`
--

LOCK TABLES `regular_hours` WRITE;
/*!40000 ALTER TABLE `regular_hours` DISABLE KEYS */;
INSERT INTO `regular_hours` VALUES ('Friday','08:00','15:00'),('Monday','08:00','22:00'),('Saturday','18:00','22:00'),('Sunday','08:00','22:00'),('Thursday','08:00','22:00'),('Tuesday','08:00','22:00'),('Wednesday','08:00','22:00');
/*!40000 ALTER TABLE `regular_hours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `reservation_number` int NOT NULL AUTO_INCREMENT,
  `reservation_date` date NOT NULL,
  `reservation_time` time NOT NULL,
  `number_of_guests` int NOT NULL,
  `verification_code` varchar(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `member_id` int DEFAULT NULL,
  `guest_full_name` varchar(100) DEFAULT NULL,
  `guest_phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `reminder_sent` tinyint DEFAULT '0',
  `status` enum('pending','approved','seated','cancelled','no_show') DEFAULT 'pending',
  PRIMARY KEY (`reservation_number`),
  UNIQUE KEY `verification_code` (`verification_code`),
  KEY `fk_res_subscribers` (`member_id`),
  CONSTRAINT `fk_res_subscribers` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `special_hours`
--

DROP TABLE IF EXISTS `special_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `special_hours` (
  `special_date` date NOT NULL,
  `open_time` varchar(5) DEFAULT NULL,
  `close_time` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`special_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `special_hours`
--

LOCK TABLES `special_hours` WRITE;
/*!40000 ALTER TABLE `special_hours` DISABLE KEYS */;
/*!40000 ALTER TABLE `special_hours` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (1,'manager','pass123','Daniil Hessen','manager'),(2,'worker','pass123','Bob Smith','worker');
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
  `member_id` int DEFAULT NULL,
  `verification_code` varchar(20) NOT NULL,
  `guest_full_name` varchar(100) DEFAULT NULL,
  `guest_phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `party_size` int DEFAULT NULL,
  `start_time` timestamp NULL DEFAULT NULL,
  `end_time` timestamp NULL DEFAULT NULL,
  `is_active` tinyint DEFAULT '1',
  `bill_sent` tinyint DEFAULT '0',
  PRIMARY KEY (`visit_id`),
  UNIQUE KEY `verification_code` (`verification_code`),
  KEY `fk_session_table` (`table_id`),
  KEY `fk_session_res` (`reservation_number`),
  KEY `fk_session_wait` (`waiting_id`),
  CONSTRAINT `fk_session_res` FOREIGN KEY (`reservation_number`) REFERENCES `reservation` (`reservation_number`),
  CONSTRAINT `fk_session_table` FOREIGN KEY (`table_id`) REFERENCES `tables` (`table_id`),
  CONSTRAINT `fk_session_wait` FOREIGN KEY (`waiting_id`) REFERENCES `waiting_list` (`waiting_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visits`
--

LOCK TABLES `visits` WRITE;
/*!40000 ALTER TABLE `visits` DISABLE KEYS */;
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
  `verification_code` varchar(20) NOT NULL,
  `table_id` int DEFAULT NULL,
  `guest_full_name` varchar(100) DEFAULT NULL,
  `guest_phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `number_of_guests` int NOT NULL,
  `entered_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `notified_at` datetime DEFAULT NULL,
  `status` enum('waiting','notified','seated','cancelled') DEFAULT 'waiting',
  PRIMARY KEY (`waiting_id`),
  UNIQUE KEY `verification_code` (`verification_code`),
  KEY `fk_wait_members` (`member_id`),
  CONSTRAINT `fk_wait_members` FOREIGN KEY (`member_id`) REFERENCES `members` (`member_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
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

-- Dump completed on 2026-01-18  2:38:06
