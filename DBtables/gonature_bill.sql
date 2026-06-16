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
-- Table structure for table `bill`
--

DROP TABLE IF EXISTS `bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `visit_id` int NOT NULL,
  `bill_type` enum('private_ordered','private_unplanned','group_ordered','group_unplanned','subscriber') NOT NULL,
  `full_price` decimal(10,2) NOT NULL,
  `number_of_paid_visitors` int NOT NULL,
  `discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `base_discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `prepaid_discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `subscriber_extra_discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `promotion_discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `final_price` decimal(10,2) NOT NULL,
  `bill_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bill_id`),
  KEY `fk_bill_visit` (`visit_id`),
  CONSTRAINT `fk_bill_visit` FOREIGN KEY (`visit_id`) REFERENCES `visit` (`visit_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill`
--

LOCK TABLES `bill` WRITE;
/*!40000 ALTER TABLE `bill` DISABLE KEYS */;
INSERT INTO `bill` VALUES (1,1,'group_ordered',175.00,5,37.00,25.00,12.00,0.00,0.00,115.50,'2026-05-30 18:06:27'),(2,2,'private_ordered',80.00,2,15.00,15.00,0.00,0.00,5.00,64.60,'2026-05-30 18:06:27'),(3,3,'group_ordered',315.00,7,37.00,25.00,12.00,0.00,0.00,207.90,'2026-05-30 18:06:27'),(4,4,'private_ordered',35.00,1,15.00,15.00,0.00,0.00,0.00,29.75,'2026-05-30 18:06:27'),(5,5,'private_ordered',200.00,5,15.00,15.00,0.00,0.00,5.00,161.50,'2026-05-30 18:06:27'),(6,6,'private_ordered',225.00,5,15.00,15.00,0.00,0.00,0.00,191.25,'2026-05-30 18:06:27'),(7,7,'private_ordered',105.00,3,15.00,15.00,0.00,0.00,0.00,89.25,'2026-05-30 18:06:27'),(8,8,'private_ordered',160.00,4,15.00,15.00,0.00,10.00,5.00,116.28,'2026-05-30 18:11:49'),(9,9,'private_ordered',90.00,2,15.00,15.00,0.00,10.00,0.00,68.85,'2026-05-30 18:11:49'),(10,10,'group_ordered',175.00,5,37.00,25.00,12.00,10.00,0.00,103.95,'2026-05-30 18:11:49');
/*!40000 ALTER TABLE `bill` ENABLE KEYS */;
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
