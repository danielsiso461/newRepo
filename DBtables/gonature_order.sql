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
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `order_number` int NOT NULL AUTO_INCREMENT,
  `order_date` date DEFAULT NULL,
  `number_of_visitors` int DEFAULT NULL,
  `confirmation_code` int unsigned NOT NULL DEFAULT '0',
  `subscriber_id` int DEFAULT NULL,
  `date_of_placing_order` date DEFAULT NULL,
  `park_id` int DEFAULT NULL,
  `guide_id` int DEFAULT NULL,
  `order_status` enum('pending','approved','cancelled','expired','completed','no_show') NOT NULL DEFAULT 'pending',
  `order_type` enum('private','organized_group') NOT NULL DEFAULT 'private',
  `order_hour` int NOT NULL,
  `customer_id` int NOT NULL,
  `email` varchar(45) NOT NULL,
  PRIMARY KEY (`order_number`),
  UNIQUE KEY `confirmation_code_UNIQUE` (`confirmation_code`),
  KEY `subscriber_id_idx` (`subscriber_id`),
  KEY `fk_order_guide` (`guide_id`),
  KEY `fk_order_park_idx` (`park_id`),
  CONSTRAINT `fk_order_guide` FOREIGN KEY (`guide_id`) REFERENCES `guide` (`guide_id`),
  CONSTRAINT `fk_order_park` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`),
  CONSTRAINT `subscriber_id` FOREIGN KEY (`subscriber_id`) REFERENCES `subscriber` (`subscriber_id`),
  CONSTRAINT `chk_order_hour` CHECK ((`order_hour` between 0 and 23))
) ENGINE=InnoDB AUTO_INCREMENT=777837 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (723874,'2028-04-29',6,6666,345672184,'2028-04-06',1,101,'approved','organized_group',12,345672184,''),(723875,'2028-04-13',2,7777,456781239,'2028-04-07',2,NULL,'cancelled','private',12,456781239,''),(723876,'2028-04-14',8,8888,567892341,'2028-04-08',3,102,'approved','organized_group',12,567892341,''),(723877,'2028-04-15',1,9999,678903452,'2028-04-09',1,NULL,'approved','private',12,678903452,''),(723878,'2028-04-16',5,1234,789014563,'2028-04-10',2,NULL,'approved','private',12,789014563,''),(723879,'2028-04-17',5,4321,764937601,'2028-04-11',3,NULL,'approved','private',12,764937601,''),(723880,'2028-04-18',3,2468,215346467,'2028-04-12',1,NULL,'approved','private',21,215346467,''),(723881,'2028-04-19',4,1357,764937601,'2028-04-13',2,NULL,'approved','private',21,764937601,''),(723882,'2028-04-20',2,8642,764937601,'2028-04-14',3,NULL,'approved','private',21,764937601,''),(723883,'2028-04-28',6,9753,764937601,'2028-04-15',1,103,'approved','organized_group',21,764937601,''),(723884,'2028-04-19',7,2469,764937601,'2028-04-16',2,103,'approved','organized_group',21,764937601,''),(723885,'2026-05-16',5,1122,764937601,'2028-04-17',3,NULL,'completed','private',22,764937601,''),(777777,'2028-04-06',98,1111,764937601,'2028-04-06',1,NULL,'approved','private',11,764937601,''),(777806,'2028-04-06',1,177806,NULL,'2026-06-14',1,NULL,'approved','private',12,764937600,'@'),(777807,'2028-04-06',1,177807,NULL,'2026-06-14',1,NULL,'approved','private',12,111111111,'@'),(777808,'2028-04-06',1,177808,NULL,'2026-06-14',1,NULL,'approved','private',12,111111111,'@'),(777809,'2026-06-24',1,177809,NULL,'2026-06-14',1,NULL,'approved','private',3,111111111,'@'),(777810,'2026-06-19',1,177810,NULL,'2026-06-14',1,NULL,'approved','private',5,111111111,'@'),(777811,'2026-06-27',1,177811,NULL,'2026-06-14',1,NULL,'approved','private',6,111111111,'@'),(777812,'2026-06-27',1,177812,NULL,'2026-06-14',1,NULL,'approved','private',5,111111111,'@'),(777813,'2026-06-27',1,177813,NULL,'2026-06-14',1,NULL,'approved','private',4,111111111,'@'),(777814,'2026-06-19',1,177814,NULL,'2026-06-14',1,NULL,'approved','private',5,111111111,'@'),(777815,'2026-06-26',1,177815,NULL,'2026-06-14',2,NULL,'approved','private',1,111111111,'@'),(777816,'2026-06-19',1,177816,NULL,'2026-06-14',3,NULL,'approved','private',3,111111111,'@'),(777817,'2026-06-18',1,177817,NULL,'2026-06-14',1,NULL,'approved','private',1,111111111,'@'),(777818,'2028-04-18',13,177818,764937601,'2026-06-14',1,NULL,'approved','private',12,764937601,'@'),(777819,'2028-04-06',11,177819,764937601,'2026-06-14',1,NULL,'approved','private',16,764937601,'@'),(777828,'2026-06-26',2,177828,567892341,'2026-06-14',2,102,'approved','organized_group',3,567892341,'@'),(777829,'2026-06-26',2,177829,567892341,'2026-06-14',3,NULL,'approved','private',3,567892341,'@'),(777830,'2026-06-26',3,177830,764937601,'2026-06-14',2,NULL,'approved','private',3,764937601,'@'),(777831,'2026-06-19',1,177831,NULL,'2026-06-14',3,NULL,'approved','private',3,111111111,'@'),(777832,'2026-06-26',1,177832,NULL,'2026-06-15',1,NULL,'approved','private',3,111111111,'@'),(777833,'2026-07-03',1,177833,NULL,'2026-06-15',2,NULL,'approved','private',3,111111111,'@'),(777834,'2026-06-19',1,177834,NULL,'2026-06-15',2,NULL,'approved','private',3,111111111,'@'),(777835,'2026-06-19',1,177835,NULL,'2026-06-15',2,NULL,'approved','private',2,123123123,'@'),(777836,'2026-06-19',1,177836,NULL,'2026-06-15',3,NULL,'approved','private',2,111111111,'@');
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
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
