-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: gonature
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `park_parameter_change_request`
--

DROP TABLE IF EXISTS `park_parameter_change_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `park_parameter_change_request` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `park_id` int NOT NULL,
  `requested_by_employee_id` int NOT NULL,
  `approved_by_employee_id` int DEFAULT NULL,
  `parameter_name` enum('max_capacity','places_for_unplanned_visitors','estimated_visit_duration_hours','promotions') NOT NULL,
  `old_value` varchar(100) NOT NULL,
  `new_value` varchar(100) NOT NULL,
  `request_status` enum('pending','approved','rejected') NOT NULL DEFAULT 'pending',
  `requested_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` datetime DEFAULT NULL,
  `review_note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`request_id`),
  KEY `fk_pcr_park` (`park_id`),
  KEY `fk_pcr_requested_by` (`requested_by_employee_id`),
  KEY `fk_pcr_approved_by` (`approved_by_employee_id`),
  CONSTRAINT `fk_pcr_approved_by` FOREIGN KEY (`approved_by_employee_id`) REFERENCES `employee` (`employee_id`),
  CONSTRAINT `fk_pcr_park` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`),
  CONSTRAINT `fk_pcr_requested_by` FOREIGN KEY (`requested_by_employee_id`) REFERENCES `employee` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `park_parameter_change_request`
--

LOCK TABLES `park_parameter_change_request` WRITE;
/*!40000 ALTER TABLE `park_parameter_change_request` DISABLE KEYS */;
INSERT INTO `park_parameter_change_request` VALUES (1,1,1,4,'max_capacity','120','130','approved','2026-05-30 18:27:28','2026-05-30 18:27:40','Approved by department manager');
/*!40000 ALTER TABLE `park_parameter_change_request` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-30 18:48:00
