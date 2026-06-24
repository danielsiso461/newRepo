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
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `notification_id` int NOT NULL AUTO_INCREMENT,
  `subscriber_id` int DEFAULT NULL,
  `order_number` int DEFAULT NULL,
  `waiting_id` int DEFAULT NULL,
  `notification_type` enum('order_confirmation','visit_reminder','order_cancelled','auto_cancelled','waiting_offer','waiting_offer_expired') NOT NULL,
  `send_channel` enum('email','sms','popup') NOT NULL DEFAULT 'popup',
  `recipient_email` varchar(100) DEFAULT NULL,
  `recipient_phone` varchar(20) DEFAULT NULL,
  `message_title` varchar(150) NOT NULL,
  `message_body` text NOT NULL,
  `scheduled_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sent_at` datetime DEFAULT NULL,
  `notification_status` enum('pending','sent','failed','cancelled') NOT NULL DEFAULT 'pending',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `fk_notification_subscriber` (`subscriber_id`),
  KEY `fk_notification_order` (`order_number`),
  KEY `fk_notification_waiting` (`waiting_id`),
  CONSTRAINT `fk_notification_order` FOREIGN KEY (`order_number`) REFERENCES `order` (`order_number`),
  CONSTRAINT `fk_notification_subscriber` FOREIGN KEY (`subscriber_id`) REFERENCES `subscriber` (`subscriber_id`),
  CONSTRAINT `fk_notification_waiting` FOREIGN KEY (`waiting_id`) REFERENCES `waiting_list` (`waiting_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,345672184,723874,NULL,'order_confirmation','popup','lior.mizrahi@gmail.com','0503333333','Order Confirmation','Hello Lior Mizrahi, your visit order number 723874 to park Carmel National Park on 2028-04-29 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(2,567892341,723876,NULL,'order_confirmation','popup','tomer.bar@gmail.com','0505555555','Order Confirmation','Hello Tomer Bar, your visit order number 723876 to park Ein Gedi Nature Reserve on 2028-04-14 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(3,678903452,723877,NULL,'order_confirmation','popup','eden.yosef@gmail.com','0506666666','Order Confirmation','Hello Eden Yosef, your visit order number 723877 to park Carmel National Park on 2028-04-15 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(4,789014563,723878,NULL,'order_confirmation','popup','omer.klein@gmail.com','0508888888','Order Confirmation','Hello Omer Klein, your visit order number 723878 to park Banias Nature Reserve on 2028-04-16 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(5,764937601,723879,NULL,'order_confirmation','popup','daniel.peretz@gmail.com','0507777777','Order Confirmation','Hello Daniel Peretz, your visit order number 723879 to park Ein Gedi Nature Reserve on 2028-04-17 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(6,215346467,723880,NULL,'order_confirmation','popup','noa.levi@gmail.com','0501111111','Order Confirmation','Hello Noa Levi, your visit order number 723880 to park Carmel National Park on 2028-04-18 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(7,764937601,723881,NULL,'order_confirmation','popup','daniel.peretz@gmail.com','0507777777','Order Confirmation','Hello Daniel Peretz, your visit order number 723881 to park Banias Nature Reserve on 2028-04-19 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(8,764937601,723882,NULL,'order_confirmation','popup','daniel.peretz@gmail.com','0507777777','Order Confirmation','Hello Daniel Peretz, your visit order number 723882 to park Ein Gedi Nature Reserve on 2028-04-20 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(9,764937601,723883,NULL,'order_confirmation','popup','daniel.peretz@gmail.com','0507777777','Order Confirmation','Hello Daniel Peretz, your visit order number 723883 to park Carmel National Park on 2028-04-28 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(10,764937601,723884,NULL,'order_confirmation','popup','daniel.peretz@gmail.com','0507777777','Order Confirmation','Hello Daniel Peretz, your visit order number 723884 to park Banias Nature Reserve on 2028-04-19 has been approved.','2026-05-30 18:41:41','2026-05-30 18:41:41','sent','2026-05-30 18:41:41'),(16,345672184,723874,NULL,'visit_reminder','popup','lior.mizrahi@gmail.com','0503333333','Visit Reminder','Reminder: your visit order number 723874 to Carmel National Park is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-28 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(17,567892341,723876,NULL,'visit_reminder','popup','tomer.bar@gmail.com','0505555555','Visit Reminder','Reminder: your visit order number 723876 to Ein Gedi Nature Reserve is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-13 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(18,678903452,723877,NULL,'visit_reminder','popup','eden.yosef@gmail.com','0506666666','Visit Reminder','Reminder: your visit order number 723877 to Carmel National Park is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-14 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(19,789014563,723878,NULL,'visit_reminder','popup','omer.klein@gmail.com','0508888888','Visit Reminder','Reminder: your visit order number 723878 to Banias Nature Reserve is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-15 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(20,764937601,723879,NULL,'visit_reminder','popup','daniel.peretz@gmail.com','0507777777','Visit Reminder','Reminder: your visit order number 723879 to Ein Gedi Nature Reserve is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-16 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(21,215346467,723880,NULL,'visit_reminder','popup','noa.levi@gmail.com','0501111111','Visit Reminder','Reminder: your visit order number 723880 to Carmel National Park is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-17 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(22,764937601,723881,NULL,'visit_reminder','popup','daniel.peretz@gmail.com','0507777777','Visit Reminder','Reminder: your visit order number 723881 to Banias Nature Reserve is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-18 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(23,764937601,723882,NULL,'visit_reminder','popup','daniel.peretz@gmail.com','0507777777','Visit Reminder','Reminder: your visit order number 723882 to Ein Gedi Nature Reserve is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-19 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(24,764937601,723883,NULL,'visit_reminder','popup','daniel.peretz@gmail.com','0507777777','Visit Reminder','Reminder: your visit order number 723883 to Carmel National Park is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-27 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(25,764937601,723884,NULL,'visit_reminder','popup','daniel.peretz@gmail.com','0507777777','Visit Reminder','Reminder: your visit order number 723884 to Banias Nature Reserve is scheduled for tomorrow. Please confirm or cancel within two hours.','2028-04-18 09:00:00',NULL,'pending','2026-05-30 18:41:57'),(31,456781239,723875,NULL,'order_cancelled','popup','shira.azulay@gmail.com','0504444444','Order Cancelled','Your order number 723875 for park Banias Nature Reserve has been cancelled. Reason: Visitor cancelled the order','2026-05-30 18:37:20','2026-05-30 18:37:20','sent','2026-05-30 18:42:21'),(32,789014563,NULL,3,'waiting_offer','popup','omer.klein@gmail.com','0508888888','Waiting List Offer','Hello Omer Klein, a place is now available for your requested visit to Ein Gedi Nature Reserve on 2028-04-22 00:00:00. Please confirm before 2026-05-31 17:58:57.','2026-05-30 17:58:57','2026-05-30 17:58:57','sent','2026-05-30 18:42:34');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
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
