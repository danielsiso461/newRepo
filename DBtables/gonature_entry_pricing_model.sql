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
-- Table structure for table `entry_pricing_model`
--

DROP TABLE IF EXISTS `entry_pricing_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `entry_pricing_model` (
  `pricing_id` int NOT NULL AUTO_INCREMENT,
  `bill_type` enum('private_ordered','private_unplanned','group_ordered','group_unplanned','subscriber') NOT NULL,
  `pricing_name` varchar(100) NOT NULL,
  `payment_basis` enum('per_visitor') NOT NULL DEFAULT 'per_visitor',
  `base_discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `prepaid_discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `subscriber_extra_discount_percent` decimal(5,2) NOT NULL DEFAULT '0.00',
  `guide_pays` tinyint(1) NOT NULL DEFAULT '1',
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`pricing_id`),
  UNIQUE KEY `uq_entry_pricing_bill_type` (`bill_type`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entry_pricing_model`
--

LOCK TABLES `entry_pricing_model` WRITE;
/*!40000 ALTER TABLE `entry_pricing_model` DISABLE KEYS */;
INSERT INTO `entry_pricing_model` VALUES (1,'private_ordered','Private / family visit ordered in advance','per_visitor',15.00,0.00,0.00,1,'Personal or family visit ordered in advance: 15% discount from full price.'),(2,'private_unplanned','Private / family unplanned visit','per_visitor',0.00,0.00,0.00,1,'Unplanned personal or family visit: full price.'),(3,'group_ordered','Organized group ordered in advance','per_visitor',25.00,12.00,0.00,0,'Organized group ordered in advance: 25% discount. Additional 12% for prepaid payment. Guide does not pay.'),(4,'group_unplanned','Unplanned organized group visit','per_visitor',10.00,0.00,0.00,1,'Unplanned organized group visit: 10% discount. Guide pays.'),(5,'subscriber','Subscriber tariff','per_visitor',0.00,0.00,10.00,1,'Subscriber gets an additional 10% discount, including combined discounts.');
/*!40000 ALTER TABLE `entry_pricing_model` ENABLE KEYS */;
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
