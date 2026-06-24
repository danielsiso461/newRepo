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
/*!50001 VIEW `cancellation_report` AS select `osh`.`history_id` AS `history_id`,`osh`.`order_number` AS `order_number`,`s`.`subscriber_name` AS `subscriber_name`,`o`.`order_date` AS `order_date`,`o`.`park_id` AS `park_id`,`p`.`park_name` AS `park_name`,`osh`.`old_status` AS `old_status`,`osh`.`new_status` AS `new_status`,`osh`.`changed_at` AS `changed_at`,`osh`.`change_reason` AS `change_reason` from (((`order_status_history` `osh` join `order` `o` on((`osh`.`order_number` = `o`.`order_number`))) join `subscriber` `s` on((`o`.`subscriber_id` = `s`.`subscriber_id`))) join `park` `p` on((`o`.`park_id` = `p`.`park_id`))) where (`osh`.`new_status` in ('cancelled','expired','no_show')) */;
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

--
-- Dumping events for database 'gonature'
--

--
-- Dumping routines for database 'gonature'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-15 19:12:45
