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
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `order_number` int NOT NULL,
  `order_date` date DEFAULT NULL,
  `number_of_visitors` int DEFAULT NULL,
  `confirmation_code` int DEFAULT NULL,
  `subscriber_id` int DEFAULT NULL,
  `date_of_placing_order` date DEFAULT NULL,
  `park_id` int DEFAULT NULL,
  `guide_id` int DEFAULT NULL,
  `order_status` enum('pending','approved','cancelled','expired','completed','no_show') NOT NULL DEFAULT 'pending',
  `order_type` enum('private','organized_group') NOT NULL DEFAULT 'private',
  PRIMARY KEY (`order_number`),
  KEY `subscriber_id_idx` (`subscriber_id`),
  KEY `fk_order_guide` (`guide_id`),
  KEY `fk_order_park_idx` (`park_id`),
  CONSTRAINT `fk_order_guide` FOREIGN KEY (`guide_id`) REFERENCES `guide` (`guide_id`),
  CONSTRAINT `fk_order_park` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`),
  CONSTRAINT `subscriber_id` FOREIGN KEY (`subscriber_id`) REFERENCES `subscriber` (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (723874,'2028-04-29',6,6666,345672184,'2028-04-06',1,101,'approved','organized_group'),(723875,'2028-04-13',2,7777,456781239,'2028-04-07',2,NULL,'cancelled','private'),(723876,'2028-04-14',8,8888,567892341,'2028-04-08',3,102,'approved','organized_group'),(723877,'2028-04-15',1,9999,678903452,'2028-04-09',1,NULL,'approved','private'),(723878,'2028-04-16',5,1234,789014563,'2028-04-10',2,NULL,'approved','private'),(723879,'2028-04-17',5,4321,764937601,'2028-04-11',3,NULL,'approved','private'),(723880,'2028-04-18',3,2468,215346467,'2028-04-12',1,NULL,'approved','private'),(723881,'2028-04-19',4,1357,764937601,'2028-04-13',2,NULL,'approved','private'),(723882,'2028-04-20',2,8642,764937601,'2028-04-14',3,NULL,'approved','private'),(723883,'2028-04-28',6,9753,764937601,'2028-04-15',1,103,'approved','organized_group'),(723884,'2028-04-19',7,2468,764937601,'2028-04-16',2,103,'approved','organized_group'),(723885,'2026-05-16',5,1122,764937601,'2028-04-17',3,NULL,'completed','private');
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

-- Dump completed on 2026-05-30 18:48:00
