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
-- Table structure for table `subscriber`
--

DROP TABLE IF EXISTS `subscriber`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subscriber` (
  `subscriber_id` int NOT NULL,
  `subscriber_name` varchar(45) DEFAULT NULL,
  `subscriber_id_number` varchar(20) DEFAULT NULL,
  `subscriber_phone` varchar(20) DEFAULT NULL,
  `subscriber_email` varchar(100) DEFAULT NULL,
  `family_members_count` int NOT NULL DEFAULT '1',
  `payment_method` enum('cash','credit_card') NOT NULL DEFAULT 'cash',
  `credit_card_last4` varchar(4) DEFAULT NULL,
  PRIMARY KEY (`subscriber_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber`
--

LOCK TABLES `subscriber` WRITE;
/*!40000 ALTER TABLE `subscriber` DISABLE KEYS */;
INSERT INTO `subscriber` VALUES (215346467,'Noa Levi','215346467','0501111111','noa.levi@gmail.com',3,'credit_card','1763'),(257218593,'Amit Cohen','257218593','0502222222','amit.cohen@gmail.com',1,'cash','1598'),(345672184,'Lior Mizrahi','345672184','0503333333','lior.mizrahi@gmail.com',4,'credit_card','2558'),(456781239,'Shira Azulay','456781239','0504444444','shira.azulay@gmail.com',2,'cash','5548'),(567892341,'Tomer Bar','567892341','0505555555','tomer.bar@gmail.com',5,'credit_card','1763'),(678903452,'Eden Yosef','678903452','0506666666','eden.yosef@gmail.com',1,'cash','1485'),(764937601,'Daniel Peretz','764937601','0507777777','daniel.peretz@gmail.com',3,'credit_card','1456'),(789014563,'Omer Klein','789014563','0508888888','omer.klein@gmail.com',2,'cash','3294');
/*!40000 ALTER TABLE `subscriber` ENABLE KEYS */;
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
