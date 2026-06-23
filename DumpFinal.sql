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

--
-- Temporary view structure for view `cancellation_report`
--

DROP TABLE IF EXISTS `cancellation_report`;
/*!50001 DROP VIEW IF EXISTS `cancellation_report`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `cancellation_report` AS SELECT 
 1 AS `history_id`,
 1 AS `order_number`,
 1 AS `subscriber_name`,
 1 AS `order_date`,
 1 AS `park_id`,
 1 AS `park_name`,
 1 AS `old_status`,
 1 AS `new_status`,
 1 AS `changed_at`,
 1 AS `change_reason`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `employee`
--

DROP TABLE IF EXISTS `employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `employee_number` varchar(30) NOT NULL,
  `employee_first_name` varchar(50) NOT NULL,
  `employee_last_name` varchar(50) NOT NULL,
  `employee_email` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `employee_role` enum('park_worker','park_manager','department_manager','service_representative') NOT NULL,
  `park_id` int DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `employee_number` (`employee_number`),
  UNIQUE KEY `employee_email` (`employee_email`),
  UNIQUE KEY `username` (`username`),
  KEY `fk_employee_park` (`park_id`),
  CONSTRAINT `fk_employee_park` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'1001','Dana','Cohen','dana.cohen@gonature.com','dana.cohen','1234','park_manager',1,1),(2,'1002','Yossi','Levi','yossi.levi@gonature.com','yossi.levi','1234','park_worker',2,1),(3,'1003','Maya','Bar','maya.bar@gonature.com','maya.bar','1234','park_worker',3,1),(4,'1004','Avi','Soffer','avi.soffer@gonature.com','avi.soffer','1234','department_manager',NULL,1);
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

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

--
-- Table structure for table `guide`
--

DROP TABLE IF EXISTS `guide`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `guide` (
  `guide_id` int NOT NULL AUTO_INCREMENT,
  `subscriber_id` int NOT NULL,
  `authorized_by_employee_id` int NOT NULL,
  `organization_name` varchar(100) DEFAULT NULL,
  `guide_status` enum('active','revoked') NOT NULL DEFAULT 'active',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`guide_id`),
  KEY `fk_guide_subscriber` (`subscriber_id`),
  KEY `fk_guide_employee` (`authorized_by_employee_id`),
  CONSTRAINT `fk_guide_employee` FOREIGN KEY (`authorized_by_employee_id`) REFERENCES `employee` (`employee_id`),
  CONSTRAINT `fk_guide_subscriber` FOREIGN KEY (`subscriber_id`) REFERENCES `subscriber` (`subscriber_id`)
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guide`
--

LOCK TABLES `guide` WRITE;
/*!40000 ALTER TABLE `guide` DISABLE KEYS */;
INSERT INTO `guide` VALUES (101,345672184,1,'Galil Tours','active','2026-05-30 17:48:26'),(102,567892341,1,'Nature Kids Group','active','2026-05-30 17:48:26'),(103,764937601,1,'GoNature Organized Trips','revoked','2026-05-30 17:48:26');
/*!40000 ALTER TABLE `guide` ENABLE KEYS */;
UNLOCK TABLES;

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

--
-- Temporary view structure for view `notification_report`
--

DROP TABLE IF EXISTS `notification_report`;
/*!50001 DROP VIEW IF EXISTS `notification_report`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `notification_report` AS SELECT 
 1 AS `notification_id`,
 1 AS `notification_type`,
 1 AS `send_channel`,
 1 AS `subscriber_name`,
 1 AS `subscriber_email`,
 1 AS `subscriber_phone`,
 1 AS `order_number`,
 1 AS `waiting_id`,
 1 AS `message_title`,
 1 AS `message_body`,
 1 AS `scheduled_at`,
 1 AS `sent_at`,
 1 AS `notification_status`,
 1 AS `created_at`*/;
SET character_set_client = @saved_cs_client;

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
) ENGINE=InnoDB AUTO_INCREMENT=777927 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (723874,'2028-04-29',6,6666,345672184,'2028-04-06',1,101,'approved','organized_group',12,345672184,''),(723875,'2028-04-13',2,7777,456781239,'2028-04-07',2,NULL,'cancelled','private',12,456781239,''),(723876,'2028-04-14',8,8888,567892341,'2028-04-08',3,102,'approved','organized_group',12,567892341,''),(723877,'2028-04-15',1,9999,678903452,'2028-04-09',1,NULL,'approved','private',12,678903452,''),(723878,'2028-04-16',5,1234,789014563,'2028-04-10',2,NULL,'approved','private',12,789014563,''),(723879,'2028-04-17',5,4321,764937601,'2028-04-11',3,NULL,'approved','private',12,764937601,''),(723880,'2028-04-18',3,2468,215346467,'2028-04-12',1,NULL,'approved','private',21,215346467,''),(723881,'2028-04-19',4,1357,764937601,'2028-04-13',2,NULL,'approved','private',21,764937601,''),(723882,'2028-04-20',2,8642,764937601,'2028-04-14',3,NULL,'approved','private',21,764937601,''),(723883,'2028-04-28',6,9753,764937601,'2028-04-15',1,103,'approved','organized_group',21,764937601,''),(723884,'2028-04-19',7,2469,764937601,'2028-04-16',2,103,'approved','organized_group',21,764937601,''),(723885,'2026-05-16',5,1122,764937601,'2028-04-17',3,NULL,'completed','private',22,764937601,''),(777777,'2028-04-06',14,1111,764937601,'2028-04-06',1,NULL,'approved','private',11,764937601,''),(777806,'2028-04-06',1,177806,NULL,'2026-06-14',1,NULL,'approved','private',12,764937600,'@'),(777807,'2028-04-06',1,177807,NULL,'2026-06-14',1,NULL,'approved','private',12,111111111,'@'),(777808,'2028-04-06',1,177808,NULL,'2026-06-14',1,NULL,'approved','private',12,111111111,'@'),(777809,'2026-06-24',1,177809,NULL,'2026-06-14',1,NULL,'approved','private',3,111111111,'@'),(777810,'2026-06-19',1,177810,NULL,'2026-06-14',1,NULL,'approved','private',5,111111111,'@'),(777811,'2026-06-27',1,177811,NULL,'2026-06-14',1,NULL,'approved','private',6,111111111,'@'),(777812,'2026-06-27',1,177812,NULL,'2026-06-14',1,NULL,'approved','private',5,111111111,'@'),(777813,'2026-06-27',1,177813,NULL,'2026-06-14',1,NULL,'approved','private',4,111111111,'@'),(777814,'2026-06-19',1,177814,NULL,'2026-06-14',1,NULL,'approved','private',5,111111111,'@'),(777815,'2026-06-26',1,177815,NULL,'2026-06-14',2,NULL,'approved','private',1,111111111,'@'),(777816,'2026-06-19',1,177816,NULL,'2026-06-14',3,NULL,'approved','private',3,111111111,'@'),(777817,'2026-06-18',1,177817,NULL,'2026-06-14',1,NULL,'approved','private',1,111111111,'@'),(777818,'2028-04-18',11,177818,764937601,'2026-06-14',1,NULL,'approved','private',12,764937601,'@'),(777819,'2028-04-06',11,177819,764937601,'2026-06-14',1,NULL,'approved','private',16,764937601,'@'),(777828,'2026-06-26',2,177828,567892341,'2026-06-14',2,102,'approved','organized_group',3,567892341,'@'),(777829,'2026-06-26',2,177829,567892341,'2026-06-14',3,NULL,'approved','private',3,567892341,'@'),(777830,'2026-06-26',3,177830,764937601,'2026-06-14',2,NULL,'approved','private',3,764937601,'@'),(777831,'2026-06-19',1,177831,NULL,'2026-06-14',3,NULL,'approved','private',3,111111111,'@'),(777832,'2026-06-26',1,177832,NULL,'2026-06-15',1,NULL,'approved','private',3,111111111,'@'),(777833,'2026-07-03',1,177833,NULL,'2026-06-15',2,NULL,'approved','private',3,111111111,'@'),(777834,'2026-06-19',1,177834,NULL,'2026-06-15',2,NULL,'approved','private',3,111111111,'@'),(777835,'2026-06-19',1,177835,NULL,'2026-06-15',2,NULL,'approved','private',2,123123123,'@'),(777836,'2026-06-19',1,177836,NULL,'2026-06-15',3,NULL,'approved','private',2,111111111,'@'),(777837,'2026-06-18',3,177837,345672184,'2026-06-16',2,101,'approved','organized_group',2,345672184,'fayikd@ret'),(777838,'2028-04-12',9,880001,215346467,'2028-04-01',2,101,'approved','organized_group',10,215346467,'demo.group@gonature.com'),(777839,'2028-04-22',5,880101,215346467,'2028-04-01',2,NULL,'cancelled','private',10,215346467,'cancelled.demo@gonature.com'),(777840,'2028-04-25',8,880102,215346467,'2028-04-03',2,NULL,'expired','private',12,215346467,'expired.demo@gonature.com'),(777841,'2028-04-28',6,880103,215346467,'2028-04-05',2,NULL,'no_show','private',11,215346467,'noshow.demo@gonature.com'),(777842,'2028-04-03',5,910001,215346467,'2028-03-20',1,NULL,'approved','private',9,215346467,'demo.report.910001@gonature.com'),(777843,'2028-04-06',3,910002,215346467,'2028-03-23',1,NULL,'approved','private',12,215346467,'demo.report.910002@gonature.com'),(777844,'2028-04-08',18,910003,215346467,'2028-03-25',1,101,'approved','organized_group',10,215346467,'demo.report.910003@gonature.com'),(777845,'2028-04-19',12,910004,215346467,'2028-04-05',1,101,'approved','organized_group',9,215346467,'demo.report.910004@gonature.com'),(777846,'2028-04-04',6,910101,215346467,'2028-03-21',2,NULL,'approved','private',9,215346467,'demo.report.910101@gonature.com'),(777847,'2028-04-11',2,910102,215346467,'2028-03-28',2,NULL,'approved','private',14,215346467,'demo.report.910102@gonature.com'),(777848,'2028-04-12',15,910103,215346467,'2028-03-29',2,101,'approved','organized_group',10,215346467,'demo.report.910103@gonature.com'),(777849,'2028-04-18',9,910104,215346467,'2028-04-04',2,101,'approved','organized_group',8,215346467,'demo.report.910104@gonature.com'),(777850,'2028-04-02',4,910201,215346467,'2028-03-19',3,NULL,'approved','private',8,215346467,'demo.report.910201@gonature.com'),(777851,'2028-04-17',7,910202,215346467,'2028-04-03',3,NULL,'approved','private',11,215346467,'demo.report.910202@gonature.com'),(777852,'2028-04-10',20,910203,215346467,'2028-03-27',3,101,'approved','organized_group',9,215346467,'demo.report.910203@gonature.com'),(777853,'2028-04-24',11,910204,215346467,'2028-04-10',3,101,'approved','organized_group',10,215346467,'demo.report.910204@gonature.com'),(777854,'2028-04-20',5,920001,215346467,'2028-03-31',1,NULL,'cancelled','private',10,215346467,'demo.report.920001@gonature.com'),(777855,'2028-04-22',8,920002,215346467,'2028-04-02',1,NULL,'expired','private',10,215346467,'demo.report.920002@gonature.com'),(777856,'2028-04-25',3,920003,215346467,'2028-04-05',1,NULL,'no_show','private',10,215346467,'demo.report.920003@gonature.com'),(777857,'2028-04-18',4,920101,215346467,'2028-03-29',2,NULL,'cancelled','private',10,215346467,'demo.report.920101@gonature.com'),(777858,'2028-04-23',6,920102,215346467,'2028-04-03',2,NULL,'expired','private',10,215346467,'demo.report.920102@gonature.com'),(777859,'2028-04-28',9,920103,215346467,'2028-04-08',2,NULL,'no_show','private',10,215346467,'demo.report.920103@gonature.com'),(777860,'2028-04-19',7,920201,215346467,'2028-03-30',3,NULL,'cancelled','private',10,215346467,'demo.report.920201@gonature.com'),(777861,'2028-04-21',10,920202,215346467,'2028-04-01',3,NULL,'expired','private',10,215346467,'demo.report.920202@gonature.com'),(777862,'2028-04-26',5,920203,215346467,'2028-04-06',3,NULL,'no_show','private',10,215346467,'demo.report.920203@gonature.com'),(777863,'2028-04-04',8,930001,215346467,'2028-03-25',1,NULL,'approved','private',10,215346467,'demo.report.extra.930001@gonature.com'),(777864,'2028-04-14',4,930002,215346467,'2028-04-04',1,NULL,'approved','private',9,215346467,'demo.report.extra.930002@gonature.com'),(777865,'2028-04-15',16,930003,215346467,'2028-04-05',1,101,'approved','organized_group',11,215346467,'demo.report.extra.930003@gonature.com'),(777866,'2028-04-23',10,930004,215346467,'2028-04-13',1,101,'approved','organized_group',8,215346467,'demo.report.extra.930004@gonature.com'),(777867,'2028-04-05',7,930101,215346467,'2028-03-26',2,NULL,'approved','private',9,215346467,'demo.report.extra.930101@gonature.com'),(777868,'2028-04-13',3,930102,215346467,'2028-04-03',2,NULL,'approved','private',15,215346467,'demo.report.extra.930102@gonature.com'),(777869,'2028-04-21',14,930103,215346467,'2028-04-11',2,101,'approved','organized_group',10,215346467,'demo.report.extra.930103@gonature.com'),(777870,'2028-04-25',11,930104,215346467,'2028-04-15',2,101,'approved','organized_group',9,215346467,'demo.report.extra.930104@gonature.com'),(777871,'2028-04-06',6,930201,215346467,'2028-03-27',3,NULL,'approved','private',10,215346467,'demo.report.extra.930201@gonature.com'),(777872,'2028-04-18',9,930202,215346467,'2028-04-08',3,NULL,'approved','private',13,215346467,'demo.report.extra.930202@gonature.com'),(777873,'2028-04-20',17,930203,215346467,'2028-04-10',3,101,'approved','organized_group',9,215346467,'demo.report.extra.930203@gonature.com'),(777874,'2028-04-28',13,930204,215346467,'2028-04-18',3,101,'approved','organized_group',8,215346467,'demo.report.extra.930204@gonature.com'),(777875,'2028-04-21',6,940001,215346467,'2028-04-03',1,NULL,'cancelled','private',10,215346467,'demo.report.extra.940001@gonature.com'),(777876,'2028-04-24',3,940002,215346467,'2028-04-06',1,NULL,'cancelled','private',10,215346467,'demo.report.extra.940002@gonature.com'),(777877,'2028-04-26',7,940003,215346467,'2028-04-08',1,NULL,'expired','private',10,215346467,'demo.report.extra.940003@gonature.com'),(777878,'2028-04-29',5,940004,215346467,'2028-04-11',1,NULL,'expired','private',10,215346467,'demo.report.extra.940004@gonature.com'),(777879,'2028-04-27',4,940005,215346467,'2028-04-09',1,NULL,'no_show','private',10,215346467,'demo.report.extra.940005@gonature.com'),(777880,'2028-04-30',2,940006,215346467,'2028-04-12',1,NULL,'no_show','private',10,215346467,'demo.report.extra.940006@gonature.com'),(777881,'2028-04-20',5,940101,215346467,'2028-04-02',2,NULL,'cancelled','private',10,215346467,'demo.report.extra.940101@gonature.com'),(777882,'2028-04-25',8,940102,215346467,'2028-04-07',2,NULL,'cancelled','private',10,215346467,'demo.report.extra.940102@gonature.com'),(777883,'2028-04-27',6,940103,215346467,'2028-04-09',2,NULL,'expired','private',10,215346467,'demo.report.extra.940103@gonature.com'),(777884,'2028-04-30',4,940104,215346467,'2028-04-12',2,NULL,'expired','private',10,215346467,'demo.report.extra.940104@gonature.com'),(777885,'2028-04-26',7,940105,215346467,'2028-04-08',2,NULL,'no_show','private',10,215346467,'demo.report.extra.940105@gonature.com'),(777886,'2028-04-29',10,940106,215346467,'2028-04-11',2,NULL,'no_show','private',10,215346467,'demo.report.extra.940106@gonature.com'),(777887,'2028-04-22',4,940201,215346467,'2028-04-04',3,NULL,'cancelled','private',10,215346467,'demo.report.extra.940201@gonature.com'),(777888,'2028-04-28',9,940202,215346467,'2028-04-10',3,NULL,'cancelled','private',10,215346467,'demo.report.extra.940202@gonature.com'),(777889,'2028-04-24',8,940203,215346467,'2028-04-06',3,NULL,'expired','private',10,215346467,'demo.report.extra.940203@gonature.com'),(777890,'2028-04-30',6,940204,215346467,'2028-04-12',3,NULL,'expired','private',10,215346467,'demo.report.extra.940204@gonature.com'),(777891,'2028-04-25',3,940205,215346467,'2028-04-07',3,NULL,'no_show','private',10,215346467,'demo.report.extra.940205@gonature.com'),(777892,'2028-04-30',5,940206,215346467,'2028-04-12',3,NULL,'no_show','private',10,215346467,'demo.report.extra.940206@gonature.com'),(777893,'2028-04-01',30,950001,215346467,'2028-03-20',1,101,'approved','organized_group',9,215346467,'demo.pattern.950001@gonature.com'),(777894,'2028-04-03',26,950002,215346467,'2028-03-22',1,101,'approved','organized_group',10,215346467,'demo.pattern.950002@gonature.com'),(777895,'2028-04-07',32,950003,215346467,'2028-03-26',1,101,'approved','organized_group',8,215346467,'demo.pattern.950003@gonature.com'),(777896,'2028-04-12',24,950004,215346467,'2028-03-31',1,101,'approved','organized_group',11,215346467,'demo.pattern.950004@gonature.com'),(777897,'2028-04-19',28,950005,215346467,'2028-04-07',1,101,'approved','organized_group',9,215346467,'demo.pattern.950005@gonature.com'),(777898,'2028-04-24',22,950006,215346467,'2028-04-12',1,101,'approved','organized_group',10,215346467,'demo.pattern.950006@gonature.com'),(777899,'2028-04-05',4,950007,215346467,'2028-03-24',1,NULL,'approved','private',10,215346467,'demo.pattern.950007@gonature.com'),(777900,'2028-04-14',3,950008,215346467,'2028-04-02',1,NULL,'approved','private',12,215346467,'demo.pattern.950008@gonature.com'),(777901,'2028-04-06',5,950101,215346467,'2028-03-25',2,NULL,'approved','private',9,215346467,'demo.pattern.950101@gonature.com'),(777902,'2028-04-20',8,950102,215346467,'2028-04-08',2,101,'approved','organized_group',10,215346467,'demo.pattern.950102@gonature.com'),(777903,'2028-04-02',7,950201,215346467,'2028-03-21',3,NULL,'approved','private',9,215346467,'demo.pattern.950201@gonature.com'),(777904,'2028-04-13',5,950202,215346467,'2028-04-01',3,NULL,'approved','private',12,215346467,'demo.pattern.950202@gonature.com'),(777905,'2028-04-06',14,950203,215346467,'2028-03-25',3,101,'approved','organized_group',10,215346467,'demo.pattern.950203@gonature.com'),(777906,'2028-04-21',12,950204,215346467,'2028-04-09',3,101,'approved','organized_group',9,215346467,'demo.pattern.950204@gonature.com'),(777907,'2028-04-18',4,960001,215346467,'2028-03-31',1,NULL,'cancelled','private',10,215346467,'demo.pattern.960001@gonature.com'),(777908,'2028-04-22',5,960002,215346467,'2028-04-04',1,NULL,'expired','private',10,215346467,'demo.pattern.960002@gonature.com'),(777909,'2028-04-26',2,960003,215346467,'2028-04-08',1,NULL,'no_show','private',10,215346467,'demo.pattern.960003@gonature.com'),(777910,'2028-04-16',6,960101,215346467,'2028-03-29',2,NULL,'cancelled','private',10,215346467,'demo.pattern.960101@gonature.com'),(777911,'2028-04-20',7,960102,215346467,'2028-04-02',2,NULL,'cancelled','private',10,215346467,'demo.pattern.960102@gonature.com'),(777912,'2028-04-25',4,960103,215346467,'2028-04-07',2,NULL,'cancelled','private',10,215346467,'demo.pattern.960103@gonature.com'),(777913,'2028-04-23',5,960104,215346467,'2028-04-05',2,NULL,'expired','private',10,215346467,'demo.pattern.960104@gonature.com'),(777914,'2028-04-28',8,960105,215346467,'2028-04-10',2,NULL,'expired','private',10,215346467,'demo.pattern.960105@gonature.com'),(777915,'2028-04-27',3,960106,215346467,'2028-04-09',2,NULL,'no_show','private',10,215346467,'demo.pattern.960106@gonature.com'),(777916,'2028-04-11',5,960201,215346467,'2028-03-24',3,NULL,'no_show','private',10,215346467,'demo.pattern.960201@gonature.com'),(777917,'2028-04-12',4,960202,215346467,'2028-03-25',3,NULL,'no_show','private',10,215346467,'demo.pattern.960202@gonature.com'),(777918,'2028-04-15',6,960203,215346467,'2028-03-28',3,NULL,'no_show','private',10,215346467,'demo.pattern.960203@gonature.com'),(777919,'2028-04-18',8,960204,215346467,'2028-03-31',3,NULL,'no_show','private',10,215346467,'demo.pattern.960204@gonature.com'),(777920,'2028-04-20',7,960205,215346467,'2028-04-02',3,NULL,'no_show','private',10,215346467,'demo.pattern.960205@gonature.com'),(777921,'2028-04-24',3,960206,215346467,'2028-04-06',3,NULL,'no_show','private',10,215346467,'demo.pattern.960206@gonature.com'),(777922,'2028-04-27',9,960207,215346467,'2028-04-09',3,NULL,'no_show','private',10,215346467,'demo.pattern.960207@gonature.com'),(777923,'2028-04-30',10,960208,215346467,'2028-04-12',3,NULL,'no_show','private',10,215346467,'demo.pattern.960208@gonature.com'),(777924,'2028-04-23',5,960209,215346467,'2028-04-05',3,NULL,'cancelled','private',10,215346467,'demo.pattern.960209@gonature.com'),(777925,'2028-04-26',4,960210,215346467,'2028-04-08',3,NULL,'expired','private',10,215346467,'demo.pattern.960210@gonature.com'),(777926,'2026-06-25',1,177926,764937601,'2026-06-17',2,NULL,'approved','private',2,764937601,'@');
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `park`
--

DROP TABLE IF EXISTS `park`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `park` (
  `park_id` int NOT NULL AUTO_INCREMENT,
  `park_name` varchar(100) NOT NULL,
  `max_capacity` int NOT NULL,
  `current_visitors` int NOT NULL DEFAULT '0',
  `places_for_unplanned_visitors` int NOT NULL,
  `estimated_visit_duration_hours` int NOT NULL DEFAULT '4',
  `full_entry_price` decimal(10,2) NOT NULL,
  `is_active` tinyint NOT NULL DEFAULT '1',
  `promotions` decimal(5,2) NOT NULL DEFAULT '0.00',
  PRIMARY KEY (`park_id`),
  UNIQUE KEY `park_name_UNIQUE` (`park_name`),
  UNIQUE KEY `uk_park_max_capacity` (`max_capacity`),
  UNIQUE KEY `uk_park_unplanned_visitors` (`places_for_unplanned_visitors`),
  CONSTRAINT `chk_park_promotions_percent` CHECK (((`promotions` >= 0) and (`promotions` <= 100)))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `park`
--

LOCK TABLES `park` WRITE;
/*!40000 ALTER TABLE `park` DISABLE KEYS */;
INSERT INTO `park` VALUES (1,'Carmel National Park',110,0,15,4,65.00,1,15.00),(2,'Banias Nature Reserve',120,9,20,5,40.00,1,1.00),(3,'Ein Gedi Nature Reserve',130,0,25,6,45.00,1,0.00);
/*!40000 ALTER TABLE `park` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `park_parameter_change_request`
--

DROP TABLE IF EXISTS `park_parameter_change_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `park_parameter_change_request` (
  `request_id` int NOT NULL AUTO_INCREMENT,
  `park_id` int NOT NULL,
  `parameter_name` enum('max_capacity','places_for_unplanned_visitors','estimated_visit_duration_hours','promotions') NOT NULL,
  `old_value` varchar(100) NOT NULL,
  `new_value` varchar(100) NOT NULL,
  `request_status` enum('pending','approved','rejected') NOT NULL DEFAULT 'pending',
  PRIMARY KEY (`request_id`),
  KEY `fk_parameter_request_park` (`park_id`),
  CONSTRAINT `fk_parameter_request_park` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `park_parameter_change_request`
--

LOCK TABLES `park_parameter_change_request` WRITE;
/*!40000 ALTER TABLE `park_parameter_change_request` DISABLE KEYS */;
INSERT INTO `park_parameter_change_request` VALUES (1,1,'max_capacity','110','140','rejected');
/*!40000 ALTER TABLE `park_parameter_change_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `park_usage_report`
--

DROP TABLE IF EXISTS `park_usage_report`;
/*!50001 DROP VIEW IF EXISTS `park_usage_report`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `park_usage_report` AS SELECT 
 1 AS `visit_id`,
 1 AS `order_number`,
 1 AS `park_id`,
 1 AS `park_name`,
 1 AS `max_capacity`,
 1 AS `actual_number_of_visitors`,
 1 AS `remaining_capacity`,
 1 AS `occupancy_percent`,
 1 AS `entry_time`,
 1 AS `exit_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `park_visitor_counter_log`
--

DROP TABLE IF EXISTS `park_visitor_counter_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `park_visitor_counter_log` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `park_id` int NOT NULL,
  `employee_id` int NOT NULL,
  `action_type` enum('entry','exit') NOT NULL,
  `amount` int NOT NULL,
  `visitors_before` int NOT NULL,
  `visitors_after` int NOT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `park_id` (`park_id`),
  KEY `employee_id` (`employee_id`),
  CONSTRAINT `park_visitor_counter_log_ibfk_1` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`),
  CONSTRAINT `park_visitor_counter_log_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `park_visitor_counter_log`
--

LOCK TABLES `park_visitor_counter_log` WRITE;
/*!40000 ALTER TABLE `park_visitor_counter_log` DISABLE KEYS */;
INSERT INTO `park_visitor_counter_log` VALUES (1,2,2,'entry',9,0,9,'2026-06-21 23:38:29');
/*!40000 ALTER TABLE `park_visitor_counter_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `revenue_report_by_park`
--

DROP TABLE IF EXISTS `revenue_report_by_park`;
/*!50001 DROP VIEW IF EXISTS `revenue_report_by_park`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `revenue_report_by_park` AS SELECT 
 1 AS `park_id`,
 1 AS `park_name`,
 1 AS `number_of_bills`,
 1 AS `total_full_price`,
 1 AS `total_revenue`,
 1 AS `total_discounts`*/;
SET character_set_client = @saved_cs_client;

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
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `family_members_count` int NOT NULL DEFAULT '1',
  `payment_method` enum('cash','credit_card') NOT NULL DEFAULT 'cash',
  `credit_card_last4` varchar(4) DEFAULT NULL,
  PRIMARY KEY (`subscriber_id`),
  UNIQUE KEY `uk_subscriber_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscriber`
--

LOCK TABLES `subscriber` WRITE;
/*!40000 ALTER TABLE `subscriber` DISABLE KEYS */;
INSERT INTO `subscriber` VALUES (215346467,'Noa Levi','215346467','0501111111','noa.levi@gmail.com','noa.levi','Noa1234',3,'credit_card','1763'),(257218593,'Amit Cohen','257218593','0502222222','amit.cohen@gmail.com','amit.cohen','Amit1234',1,'cash','1598'),(345672184,'Lior Mizrahi','345672184','0503333333','lior.mizrahi@gmail.com','lior.mizrahi','Lior1234',4,'credit_card','2558'),(456781239,'Shira Azulay','456781239','0504444444','shira.azulay@gmail.com','shira.azulay','Shira1234',2,'cash','5548'),(567892341,'Tomer Bar','567892341','0505555555','tomer.bar@gmail.com','tomer.bar','Tomer1234',5,'credit_card','1763'),(678903452,'Eden Yosef','678903452','0506666666','eden.yosef@gmail.com','eden.yosef','Eden1234',1,'cash','1485'),(764937601,'Daniel Peretz','764937601','0507777777','daniel.peretz@gmail.com','daniel.peretz','Daniel1234',3,'credit_card','1456'),(789014563,'Omer Klein','789014563','0508888888','omer.klein@gmail.com','omer.klein','Omer1234',2,'cash','3294');
/*!40000 ALTER TABLE `subscriber` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `visit`
--

LOCK TABLES `visit` WRITE;
/*!40000 ALTER TABLE `visit` DISABLE KEYS */;
INSERT INTO `visit` VALUES (1,723874,1,345672184,'ordered',6,'2028-04-29 09:00:00','2028-04-29 12:00:00',1,1,'confirmation_code'),(2,723875,2,456781239,'ordered',2,'2028-04-13 10:00:00','2028-04-13 13:00:00',2,2,'confirmation_code'),(3,723876,3,567892341,'ordered',8,'2028-04-14 09:30:00','2028-04-14 13:30:00',3,3,'confirmation_code'),(4,723877,1,678903452,'ordered',1,'2028-04-15 11:00:00','2028-04-15 14:00:00',1,1,'confirmation_code'),(5,723878,2,789014563,'ordered',5,'2028-04-16 10:00:00','2028-04-16 13:00:00',2,2,'confirmation_code'),(6,723879,3,764937601,'ordered',5,'2028-04-17 09:00:00','2028-04-17 13:00:00',3,3,'confirmation_code'),(7,723880,1,215346467,'ordered',3,'2028-04-18 10:00:00','2028-04-18 13:00:00',1,1,'confirmation_code'),(8,723881,2,764937601,'ordered',4,'2028-04-19 09:00:00','2028-04-19 12:00:00',2,2,'confirmation_code'),(9,723882,3,764937601,'ordered',2,'2028-04-20 10:00:00','2028-04-20 13:00:00',3,3,'confirmation_code'),(10,723883,1,764937601,'ordered',6,'2028-04-28 09:30:00','2028-04-28 13:30:00',1,1,'confirmation_code'),(11,777838,2,215346467,'ordered',9,'2028-04-12 10:00:00','2028-04-12 13:00:00',2,2,'confirmation_code'),(12,NULL,2,215346467,'unplanned',4,'2028-04-15 11:00:00','2028-04-15 13:30:00',2,2,'id_number'),(14,777842,1,215346467,'ordered',5,'2028-04-03 09:00:00','2028-04-03 11:00:00',1,1,'confirmation_code'),(15,777843,1,215346467,'ordered',3,'2028-04-06 12:00:00','2028-04-06 13:30:00',1,1,'confirmation_code'),(16,777844,1,215346467,'ordered',18,'2028-04-08 10:00:00','2028-04-08 14:00:00',1,1,'confirmation_code'),(17,777845,1,215346467,'ordered',12,'2028-04-19 09:30:00','2028-04-19 12:30:00',1,1,'confirmation_code'),(18,NULL,1,215346467,'unplanned',7,'2028-04-05 11:00:00','2028-04-05 13:00:00',1,1,'id_number'),(19,NULL,1,215346467,'unplanned',4,'2028-04-13 13:00:00','2028-04-13 15:30:00',1,1,'id_number'),(20,777846,2,215346467,'ordered',6,'2028-04-04 09:15:00','2028-04-04 11:45:00',2,2,'confirmation_code'),(21,777847,2,215346467,'ordered',2,'2028-04-11 14:00:00','2028-04-11 15:00:00',2,2,'confirmation_code'),(22,777848,2,215346467,'ordered',15,'2028-04-12 10:00:00','2028-04-12 14:30:00',2,2,'confirmation_code'),(23,777849,2,215346467,'ordered',9,'2028-04-18 08:30:00','2028-04-18 11:30:00',2,2,'confirmation_code'),(24,NULL,2,215346467,'unplanned',5,'2028-04-07 10:30:00','2028-04-07 12:00:00',2,2,'id_number'),(25,NULL,2,215346467,'unplanned',8,'2028-04-16 12:00:00','2028-04-16 15:00:00',2,2,'id_number'),(26,777850,3,215346467,'ordered',4,'2028-04-02 08:45:00','2028-04-02 10:30:00',3,3,'confirmation_code'),(27,777851,3,215346467,'ordered',7,'2028-04-17 11:00:00','2028-04-17 13:30:00',3,3,'confirmation_code'),(28,777852,3,215346467,'ordered',20,'2028-04-10 09:00:00','2028-04-10 13:30:00',3,3,'confirmation_code'),(29,777853,3,215346467,'ordered',11,'2028-04-24 10:00:00','2028-04-24 12:30:00',3,3,'confirmation_code'),(30,NULL,3,215346467,'unplanned',6,'2028-04-09 09:45:00','2028-04-09 11:15:00',3,3,'id_number'),(31,NULL,3,215346467,'unplanned',10,'2028-04-22 14:00:00','2028-04-22 16:30:00',3,3,'id_number'),(32,777863,1,215346467,'ordered',8,'2028-04-04 10:00:00','2028-04-04 12:30:00',1,1,'confirmation_code'),(33,777864,1,215346467,'ordered',4,'2028-04-14 09:15:00','2028-04-14 10:45:00',1,1,'confirmation_code'),(34,777865,1,215346467,'ordered',16,'2028-04-15 11:00:00','2028-04-15 15:00:00',1,1,'confirmation_code'),(35,777866,1,215346467,'ordered',10,'2028-04-23 08:30:00','2028-04-23 11:30:00',1,1,'confirmation_code'),(36,NULL,1,215346467,'unplanned',9,'2028-04-20 12:00:00','2028-04-20 14:00:00',1,1,'id_number'),(37,NULL,1,215346467,'unplanned',5,'2028-04-27 13:00:00','2028-04-27 15:30:00',1,1,'id_number'),(38,777867,2,215346467,'ordered',7,'2028-04-05 09:30:00','2028-04-05 11:30:00',2,2,'confirmation_code'),(39,777868,2,215346467,'ordered',3,'2028-04-13 15:00:00','2028-04-13 16:00:00',2,2,'confirmation_code'),(40,777869,2,215346467,'ordered',14,'2028-04-21 10:00:00','2028-04-21 14:00:00',2,2,'confirmation_code'),(41,777870,2,215346467,'ordered',11,'2028-04-25 09:00:00','2028-04-25 12:30:00',2,2,'confirmation_code'),(42,NULL,2,215346467,'unplanned',6,'2028-04-22 11:30:00','2028-04-22 13:30:00',2,2,'id_number'),(43,NULL,2,215346467,'unplanned',12,'2028-04-29 12:00:00','2028-04-29 16:00:00',2,2,'id_number'),(44,777871,3,215346467,'ordered',6,'2028-04-06 10:00:00','2028-04-06 12:00:00',3,3,'confirmation_code'),(45,777872,3,215346467,'ordered',9,'2028-04-18 13:00:00','2028-04-18 16:00:00',3,3,'confirmation_code'),(46,777873,3,215346467,'ordered',17,'2028-04-20 09:30:00','2028-04-20 14:30:00',3,3,'confirmation_code'),(47,777874,3,215346467,'ordered',13,'2028-04-28 08:00:00','2028-04-28 11:00:00',3,3,'confirmation_code'),(48,NULL,3,215346467,'unplanned',8,'2028-04-16 11:00:00','2028-04-16 13:00:00',3,3,'id_number'),(49,NULL,3,215346467,'unplanned',7,'2028-04-30 14:00:00','2028-04-30 16:30:00',3,3,'id_number'),(50,777893,1,215346467,'ordered',30,'2028-04-01 09:00:00','2028-04-01 13:30:00',1,1,'confirmation_code'),(51,777894,1,215346467,'ordered',26,'2028-04-03 10:00:00','2028-04-03 14:00:00',1,1,'confirmation_code'),(52,777895,1,215346467,'ordered',32,'2028-04-07 08:30:00','2028-04-07 12:30:00',1,1,'confirmation_code'),(53,777896,1,215346467,'ordered',24,'2028-04-12 11:00:00','2028-04-12 15:00:00',1,1,'confirmation_code'),(54,777897,1,215346467,'ordered',28,'2028-04-19 09:30:00','2028-04-19 13:30:00',1,1,'confirmation_code'),(55,777898,1,215346467,'ordered',22,'2028-04-24 10:30:00','2028-04-24 14:00:00',1,1,'confirmation_code'),(56,777899,1,215346467,'ordered',4,'2028-04-05 10:00:00','2028-04-05 11:30:00',1,1,'confirmation_code'),(57,777900,1,215346467,'ordered',3,'2028-04-14 12:00:00','2028-04-14 13:30:00',1,1,'confirmation_code'),(58,NULL,1,215346467,'unplanned',2,'2028-04-28 12:00:00','2028-04-28 14:00:00',1,1,'id_number'),(59,NULL,2,215346467,'unplanned',10,'2028-04-01 09:00:00','2028-04-01 11:00:00',2,2,'id_number'),(60,NULL,2,215346467,'unplanned',8,'2028-04-04 10:00:00','2028-04-04 12:30:00',2,2,'id_number'),(61,NULL,2,215346467,'unplanned',12,'2028-04-08 11:00:00','2028-04-08 14:00:00',2,2,'id_number'),(62,NULL,2,215346467,'unplanned',9,'2028-04-11 12:00:00','2028-04-11 15:30:00',2,2,'id_number'),(63,NULL,2,215346467,'unplanned',11,'2028-04-15 13:00:00','2028-04-15 16:00:00',2,2,'id_number'),(64,NULL,2,215346467,'unplanned',7,'2028-04-18 09:30:00','2028-04-18 11:30:00',2,2,'id_number'),(65,NULL,2,215346467,'unplanned',6,'2028-04-22 10:30:00','2028-04-22 12:00:00',2,2,'id_number'),(66,NULL,2,215346467,'unplanned',13,'2028-04-27 14:00:00','2028-04-27 17:00:00',2,2,'id_number'),(67,777901,2,215346467,'ordered',5,'2028-04-06 09:00:00','2028-04-06 10:30:00',2,2,'confirmation_code'),(68,777902,2,215346467,'ordered',8,'2028-04-20 10:00:00','2028-04-20 13:00:00',2,2,'confirmation_code'),(69,777903,3,215346467,'ordered',7,'2028-04-02 09:00:00','2028-04-02 11:30:00',3,3,'confirmation_code'),(70,777904,3,215346467,'ordered',5,'2028-04-13 12:00:00','2028-04-13 14:00:00',3,3,'confirmation_code'),(71,777905,3,215346467,'ordered',14,'2028-04-06 10:00:00','2028-04-06 14:00:00',3,3,'confirmation_code'),(72,777906,3,215346467,'ordered',12,'2028-04-21 09:30:00','2028-04-21 13:00:00',3,3,'confirmation_code'),(73,NULL,3,215346467,'unplanned',6,'2028-04-09 13:00:00','2028-04-09 15:00:00',3,3,'id_number'),(74,NULL,2,NULL,'unplanned',18,'2028-04-02 09:00:00','2028-04-02 11:30:00',2,2,'id_number'),(75,NULL,2,NULL,'unplanned',25,'2028-04-04 10:00:00','2028-04-04 13:00:00',2,2,'id_number'),(76,NULL,2,NULL,'unplanned',32,'2028-04-07 08:30:00','2028-04-07 12:00:00',2,2,'id_number'),(77,NULL,2,NULL,'unplanned',40,'2028-04-12 11:00:00','2028-04-12 14:00:00',2,2,'id_number'),(78,NULL,2,NULL,'unplanned',28,'2028-04-20 09:30:00','2028-04-20 12:30:00',2,2,'id_number'),(79,NULL,3,NULL,'unplanned',12,'2028-04-03 09:15:00','2028-04-03 11:45:00',3,3,'id_number'),(80,NULL,3,NULL,'unplanned',20,'2028-04-06 10:00:00','2028-04-06 13:00:00',3,3,'id_number'),(81,NULL,3,NULL,'unplanned',35,'2028-04-10 08:45:00','2028-04-10 12:15:00',3,3,'id_number'),(82,NULL,3,NULL,'unplanned',45,'2028-04-18 11:00:00','2028-04-18 15:00:00',3,3,'id_number'),(83,NULL,3,NULL,'unplanned',26,'2028-04-24 09:30:00','2028-04-24 12:00:00',3,3,'id_number');
/*!40000 ALTER TABLE `visit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `visit_duration_report`
--

DROP TABLE IF EXISTS `visit_duration_report`;
/*!50001 DROP VIEW IF EXISTS `visit_duration_report`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `visit_duration_report` AS SELECT 
 1 AS `visit_id`,
 1 AS `order_number`,
 1 AS `park_name`,
 1 AS `subscriber_name`,
 1 AS `order_type`,
 1 AS `actual_number_of_visitors`,
 1 AS `entry_time`,
 1 AS `exit_time`,
 1 AS `duration_minutes`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `visit_price_calculation`
--

DROP TABLE IF EXISTS `visit_price_calculation`;
/*!50001 DROP VIEW IF EXISTS `visit_price_calculation`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `visit_price_calculation` AS SELECT 
 1 AS `visit_id`,
 1 AS `order_number`,
 1 AS `park_id`,
 1 AS `park_name`,
 1 AS `full_entry_price`,
 1 AS `promotions`,
 1 AS `subscriber_id`,
 1 AS `subscriber_name`,
 1 AS `order_type`,
 1 AS `visit_type`,
 1 AS `calculated_bill_type`,
 1 AS `actual_number_of_visitors`,
 1 AS `number_of_paid_visitors`,
 1 AS `base_discount_percent`,
 1 AS `prepaid_discount_percent`,
 1 AS `promotion_discount_percent`,
 1 AS `full_price`,
 1 AS `final_price`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `visitor_report_by_type`
--

DROP TABLE IF EXISTS `visitor_report_by_type`;
/*!50001 DROP VIEW IF EXISTS `visitor_report_by_type`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `visitor_report_by_type` AS SELECT 
 1 AS `park_id`,
 1 AS `park_name`,
 1 AS `order_type`,
 1 AS `number_of_visits`,
 1 AS `total_visitors`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `waiting_list`
--

DROP TABLE IF EXISTS `waiting_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiting_list` (
  `waiting_id` int NOT NULL AUTO_INCREMENT,
  `subscriber_id` int NOT NULL,
  `park_id` int NOT NULL,
  `requested_order_date` datetime NOT NULL,
  `number_of_visitors` int NOT NULL,
  `queue_position` int NOT NULL,
  `waiting_status` enum('waiting','offered','confirmed','expired','cancelled') NOT NULL DEFAULT 'waiting',
  `offered_at` datetime DEFAULT NULL,
  `offer_expires_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`waiting_id`),
  KEY `fk_waiting_subscriber` (`subscriber_id`),
  KEY `fk_waiting_park` (`park_id`),
  CONSTRAINT `fk_waiting_park` FOREIGN KEY (`park_id`) REFERENCES `park` (`park_id`),
  CONSTRAINT `fk_waiting_subscriber` FOREIGN KEY (`subscriber_id`) REFERENCES `subscriber` (`subscriber_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiting_list`
--

LOCK TABLES `waiting_list` WRITE;
/*!40000 ALTER TABLE `waiting_list` DISABLE KEYS */;
INSERT INTO `waiting_list` VALUES (1,215346467,1,'2028-04-20 00:00:00',3,1,'waiting',NULL,NULL,'2026-05-30 17:58:57'),(2,257218593,2,'2028-04-21 00:00:00',2,2,'waiting',NULL,NULL,'2026-05-30 17:58:57'),(3,789014563,3,'2028-04-22 00:00:00',4,3,'offered','2026-05-30 17:58:57','2026-05-31 17:58:57','2026-05-30 17:58:57');
/*!40000 ALTER TABLE `waiting_list` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `cancellation_report`
--

/*!50001 DROP VIEW IF EXISTS `cancellation_report`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `cancellation_report` AS select `o`.`order_number` AS `history_id`,`o`.`order_number` AS `order_number`,`s`.`subscriber_name` AS `subscriber_name`,`o`.`order_date` AS `order_date`,`o`.`park_id` AS `park_id`,`p`.`park_name` AS `park_name`,(case when (`o`.`order_status` = 'cancelled') then 'approved' when (`o`.`order_status` = 'expired') then 'pending' when (`o`.`order_status` = 'no_show') then 'approved' else 'pending' end) AS `old_status`,`o`.`order_status` AS `new_status`,`o`.`date_of_placing_order` AS `changed_at`,'Stored in order table' AS `change_reason` from ((`order` `o` join `subscriber` `s` on((`o`.`subscriber_id` = `s`.`subscriber_id`))) join `park` `p` on((`o`.`park_id` = `p`.`park_id`))) where (`o`.`order_status` in ('cancelled','expired','no_show')) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `notification_report`
--

/*!50001 DROP VIEW IF EXISTS `notification_report`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `notification_report` AS select `n`.`notification_id` AS `notification_id`,`n`.`notification_type` AS `notification_type`,`n`.`send_channel` AS `send_channel`,`s`.`subscriber_name` AS `subscriber_name`,`s`.`subscriber_email` AS `subscriber_email`,`s`.`subscriber_phone` AS `subscriber_phone`,`n`.`order_number` AS `order_number`,`n`.`waiting_id` AS `waiting_id`,`n`.`message_title` AS `message_title`,`n`.`message_body` AS `message_body`,`n`.`scheduled_at` AS `scheduled_at`,`n`.`sent_at` AS `sent_at`,`n`.`notification_status` AS `notification_status`,`n`.`created_at` AS `created_at` from (`notification` `n` left join `subscriber` `s` on((`n`.`subscriber_id` = `s`.`subscriber_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `park_usage_report`
--

/*!50001 DROP VIEW IF EXISTS `park_usage_report`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `park_usage_report` AS select `v`.`visit_id` AS `visit_id`,`v`.`order_number` AS `order_number`,`p`.`park_id` AS `park_id`,`p`.`park_name` AS `park_name`,`p`.`max_capacity` AS `max_capacity`,`v`.`actual_number_of_visitors` AS `actual_number_of_visitors`,(`p`.`max_capacity` - `v`.`actual_number_of_visitors`) AS `remaining_capacity`,round(((`v`.`actual_number_of_visitors` / `p`.`max_capacity`) * 100),2) AS `occupancy_percent`,`v`.`entry_time` AS `entry_time`,`v`.`exit_time` AS `exit_time` from (`visit` `v` join `park` `p` on((`v`.`park_id` = `p`.`park_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `revenue_report_by_park`
--

/*!50001 DROP VIEW IF EXISTS `revenue_report_by_park`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `revenue_report_by_park` AS select `p`.`park_id` AS `park_id`,`p`.`park_name` AS `park_name`,count(`b`.`bill_id`) AS `number_of_bills`,sum(`b`.`full_price`) AS `total_full_price`,sum(`b`.`final_price`) AS `total_revenue`,sum((`b`.`full_price` - `b`.`final_price`)) AS `total_discounts` from ((`bill` `b` join `visit` `v` on((`b`.`visit_id` = `v`.`visit_id`))) join `park` `p` on((`v`.`park_id` = `p`.`park_id`))) group by `p`.`park_id`,`p`.`park_name` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `visit_duration_report`
--

/*!50001 DROP VIEW IF EXISTS `visit_duration_report`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `visit_duration_report` AS select `v`.`visit_id` AS `visit_id`,`v`.`order_number` AS `order_number`,`p`.`park_name` AS `park_name`,`s`.`subscriber_name` AS `subscriber_name`,`o`.`order_type` AS `order_type`,`v`.`actual_number_of_visitors` AS `actual_number_of_visitors`,`v`.`entry_time` AS `entry_time`,`v`.`exit_time` AS `exit_time`,timestampdiff(MINUTE,`v`.`entry_time`,`v`.`exit_time`) AS `duration_minutes` from (((`visit` `v` join `order` `o` on((`v`.`order_number` = `o`.`order_number`))) join `park` `p` on((`v`.`park_id` = `p`.`park_id`))) join `subscriber` `s` on((`v`.`subscriber_id` = `s`.`subscriber_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `visit_price_calculation`
--

/*!50001 DROP VIEW IF EXISTS `visit_price_calculation`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `visit_price_calculation` AS select `v`.`visit_id` AS `visit_id`,`v`.`order_number` AS `order_number`,`v`.`park_id` AS `park_id`,`p`.`park_name` AS `park_name`,`p`.`full_entry_price` AS `full_entry_price`,`p`.`promotions` AS `promotions`,`v`.`subscriber_id` AS `subscriber_id`,`s`.`subscriber_name` AS `subscriber_name`,`o`.`order_type` AS `order_type`,`v`.`visit_type` AS `visit_type`,(case when ((`o`.`order_type` = 'organized_group') and (`v`.`visit_type` = 'ordered')) then 'group_ordered' when ((`o`.`order_type` = 'organized_group') and (`v`.`visit_type` = 'unplanned')) then 'group_unplanned' when ((`o`.`order_type` = 'private') and (`v`.`visit_type` = 'ordered')) then 'private_ordered' when ((`o`.`order_type` = 'private') and (`v`.`visit_type` = 'unplanned')) then 'private_unplanned' else 'private_ordered' end) AS `calculated_bill_type`,`v`.`actual_number_of_visitors` AS `actual_number_of_visitors`,(case when ((`o`.`order_type` = 'organized_group') and (`v`.`visit_type` = 'ordered')) then greatest((`v`.`actual_number_of_visitors` - 1),0) else `v`.`actual_number_of_visitors` end) AS `number_of_paid_visitors`,`ep`.`base_discount_percent` AS `base_discount_percent`,`ep`.`prepaid_discount_percent` AS `prepaid_discount_percent`,(case when (`p`.`promotions` = 1) then 5.00 else 0.00 end) AS `promotion_discount_percent`,round((`p`.`full_entry_price` * (case when ((`o`.`order_type` = 'organized_group') and (`v`.`visit_type` = 'ordered')) then greatest((`v`.`actual_number_of_visitors` - 1),0) else `v`.`actual_number_of_visitors` end)),2) AS `full_price`,round(((((`p`.`full_entry_price` * (case when ((`o`.`order_type` = 'organized_group') and (`v`.`visit_type` = 'ordered')) then greatest((`v`.`actual_number_of_visitors` - 1),0) else `v`.`actual_number_of_visitors` end)) * (1 - (`ep`.`base_discount_percent` / 100))) * (1 - (`ep`.`prepaid_discount_percent` / 100))) * (1 - ((case when (`p`.`promotions` = 1) then 5.00 else 0.00 end) / 100))),2) AS `final_price` from ((((`visit` `v` join `order` `o` on((`v`.`order_number` = `o`.`order_number`))) join `park` `p` on((`v`.`park_id` = `p`.`park_id`))) join `subscriber` `s` on((`v`.`subscriber_id` = `s`.`subscriber_id`))) join `entry_pricing_model` `ep` on((`ep`.`bill_type` = (case when ((`o`.`order_type` = 'organized_group') and (`v`.`visit_type` = 'ordered')) then 'group_ordered' when ((`o`.`order_type` = 'organized_group') and (`v`.`visit_type` = 'unplanned')) then 'group_unplanned' when ((`o`.`order_type` = 'private') and (`v`.`visit_type` = 'ordered')) then 'private_ordered' when ((`o`.`order_type` = 'private') and (`v`.`visit_type` = 'unplanned')) then 'private_unplanned' else 'private_ordered' end)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `visitor_report_by_type`
--

/*!50001 DROP VIEW IF EXISTS `visitor_report_by_type`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `visitor_report_by_type` AS select `p`.`park_id` AS `park_id`,`p`.`park_name` AS `park_name`,`o`.`order_type` AS `order_type`,count(`v`.`visit_id`) AS `number_of_visits`,sum(`v`.`actual_number_of_visitors`) AS `total_visitors` from ((`visit` `v` join `order` `o` on((`v`.`order_number` = `o`.`order_number`))) join `park` `p` on((`v`.`park_id` = `p`.`park_id`))) group by `p`.`park_id`,`p`.`park_name`,`o`.`order_type` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-23 22:05:47
