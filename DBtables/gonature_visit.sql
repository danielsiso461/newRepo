CREATE DATABASE  IF NOT EXISTS `gonature` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `gonature`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: gonature
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
-- Table structure for table `visit`
--

DROP TABLE IF EXISTS `visit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visit` (
  `visit_id` int NOT NULL AUTO_INCREMENT,
  `order_number` int DEFAULT NULL,
  `park_id` int NOT NULL,
  `subscriber_id` int DEFAULT NULL,
  `visit_type` enum('ordered','unplanned') NOT NULL,
  `actual_number_of_visitors` int NOT NULL,
  `entry_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `exit_time` datetime DEFAULT NULL,
  `handled_by_employee_id` int NOT NULL,
  `exit_handled_by_employee_id` int DEFAULT NULL,
  `identification_method` enum('id_number','confirmation_code') NOT NULL DEFAULT 'id_number',
  PRIMARY KEY (`visit_id`),
  KEY `fk_visit_order` (`order_number`),
  KEY `fk_visit_park` (`park_id`),
  KEY `fk_visit_subscriber` (`subscriber_id`),
  KEY `fk_visit_employee_entry` (`handled_by_employee_id`),
  KEY `fk_visit_employee_exit` (`exit_handled_by_employee_id`),
  CONSTRAINT `fk_visit_employee_entry` FOREIGN KEY (`handled_by_employee_id`) REFERENCES `employee` (`employee_id`),
  CONSTRAINT `fk_visit_employee_exit` FOREIGN KEY (`exit_handled_by_employee_id`) REFERENCES `employee` (`employee_id`),
  CONSTRAINT `fk_visit_order` FOREIGN KEY (`order_number`) REFERENCES `order` (`order_number`),
  CONSTRAINT `fk_visit_park` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`),
  CONSTRAINT `fk_visit_subscriber` FOREIGN KEY (`subscriber_id`) REFERENCES `subscriber` (`subscriber_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visit`
--

LOCK TABLES `visit` WRITE;
/*!40000 ALTER TABLE `visit` DISABLE KEYS */;
INSERT INTO `visit` VALUES (1,723874,1,345672184,'ordered',6,'2028-04-29 09:00:00','2028-04-29 12:00:00',1,1,'confirmation_code'),(2,723875,2,456781239,'ordered',2,'2028-04-13 10:00:00','2028-04-13 13:00:00',2,2,'confirmation_code'),(3,723876,3,567892341,'ordered',8,'2028-04-14 09:30:00','2028-04-14 13:30:00',3,3,'confirmation_code'),(4,723877,1,678903452,'ordered',1,'2028-04-15 11:00:00','2028-04-15 14:00:00',1,1,'confirmation_code'),(5,723878,2,789014563,'ordered',5,'2028-04-16 10:00:00','2028-04-16 13:00:00',2,2,'confirmation_code'),(6,723879,3,764937601,'ordered',5,'2028-04-17 09:00:00','2028-04-17 13:00:00',3,3,'confirmation_code'),(7,723880,1,215346467,'ordered',3,'2028-04-18 10:00:00','2028-04-18 13:00:00',1,1,'confirmation_code'),(8,723881,2,764937601,'ordered',4,'2028-04-19 09:00:00','2028-04-19 12:00:00',2,2,'confirmation_code'),(9,723882,3,764937601,'ordered',2,'2028-04-20 10:00:00','2028-04-20 13:00:00',3,3,'confirmation_code'),(10,723883,1,764937601,'ordered',6,'2028-04-28 09:30:00','2028-04-28 13:30:00',1,1,'confirmation_code');
/*!40000 ALTER TABLE `visit` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-15 19:12:45
