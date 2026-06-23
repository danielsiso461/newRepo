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
-- Table structure for table `order_status_history`
--

DROP TABLE IF EXISTS `order_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_status_history` (
  `history_id` int NOT NULL AUTO_INCREMENT,
  `order_number` int NOT NULL,
  `old_status` enum('pending','approved','cancelled','expired','completed','no_show') DEFAULT NULL,
  `new_status` enum('pending','approved','cancelled','expired','completed','no_show') NOT NULL,
  `changed_by_employee_id` int DEFAULT NULL,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `change_reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `fk_osh_order` (`order_number`),
  KEY `fk_osh_employee` (`changed_by_employee_id`),
  CONSTRAINT `fk_osh_employee` FOREIGN KEY (`changed_by_employee_id`) REFERENCES `employee` (`employee_id`),
  CONSTRAINT `fk_osh_order` FOREIGN KEY (`order_number`) REFERENCES `order` (`order_number`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_status_history`
--

LOCK TABLES `order_status_history` WRITE;
/*!40000 ALTER TABLE `order_status_history` DISABLE KEYS */;
INSERT INTO `order_status_history` VALUES (2,723874,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(3,723875,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(4,723876,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(5,723877,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(6,723878,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(7,723879,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(8,723880,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(9,723881,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(10,723882,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(11,723883,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(12,723884,'pending','approved',1,'2026-05-30 18:30:46','Initial status synchronization'),(13,723885,'pending','completed',1,'2026-05-30 18:30:46','Initial status synchronization'),(19,723875,'approved','cancelled',1,'2026-05-30 18:37:20','Visitor cancelled the order');
/*!40000 ALTER TABLE `order_status_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-15 19:12:44
