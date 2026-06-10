/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.11.14-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: marketanalysisdb
-- ------------------------------------------------------
-- Server version	10.11.18-MariaDB-ubu2204

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
--
-- Table structure for table `api_call_log`
--

DROP TABLE IF EXISTS `api_call_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_call_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ocurred_at` datetime(6) DEFAULT NULL,
  `ticker` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1215 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `candles`
--

DROP TABLE IF EXISTS `candles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `candles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `close_price` decimal(18,4) NOT NULL,
  `date_time` datetime(6) NOT NULL,
  `high_price` decimal(18,4) NOT NULL,
  `low_price` decimal(18,4) NOT NULL,
  `open_price` decimal(18,4) NOT NULL,
  `ticker` varchar(20) NOT NULL,
  `volume` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_candles_ticker_datetime` (`ticker`,`date_time`),
  KEY `idx_candles_ticker_datetime` (`ticker`,`date_time` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=275232 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `company_profile`
--

DROP TABLE IF EXISTS `company_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `country` varchar(255) DEFAULT NULL,
  `exchange` varchar(255) DEFAULT NULL,
  `industry` varchar(255) DEFAULT NULL,
  `ipo` varchar(255) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `market_capitalization` double DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `share_outstanding` double DEFAULT NULL,
  `ticker` varchar(255) NOT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2yxldls283avrks6s191snnp9` (`ticker`)
) ENGINE=InnoDB AUTO_INCREMENT=333 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prohibited_keywords`
--

DROP TABLE IF EXISTS `prohibited_keywords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `prohibited_keywords` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `keyword` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrv9bhv67g72jb6f5tu7wb5mxn` (`keyword`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prohibited_tickers`
--

DROP TABLE IF EXISTS `prohibited_tickers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `prohibited_tickers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `ticker` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6344agkefikqxi4srx0u5euri` (`ticker`)
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rule_definitions`
--

DROP TABLE IF EXISTS `rule_definitions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_definitions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `requires_param` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2r6k1ol40qdomntb0j5kqrjul` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rules`
--

DROP TABLE IF EXISTS `rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `operator` varchar(255) DEFAULT NULL,
  `subject_code` varchar(255) DEFAULT NULL,
  `subject_param` double DEFAULT NULL,
  `target_code` varchar(255) DEFAULT NULL,
  `target_param` double DEFAULT NULL,
  `strategy_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7kp45947ulv336emrbfar5oau` (`strategy_id`),
  CONSTRAINT `FK7kp45947ulv336emrbfar5oau` FOREIGN KEY (`strategy_id`) REFERENCES `strategies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=699 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stocks`
--

DROP TABLE IF EXISTS `stocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `stocks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `average_volume` bigint(20) DEFAULT NULL,
  `current_price` decimal(38,2) DEFAULT NULL,
  `high_of_day` decimal(38,2) DEFAULT NULL,
  `low_of_day` decimal(38,2) DEFAULT NULL,
  `open_price` decimal(38,2) DEFAULT NULL,
  `previous_close` decimal(38,2) DEFAULT NULL,
  `sma20` decimal(38,2) DEFAULT NULL,
  `sma200` decimal(38,2) DEFAULT NULL,
  `sma50` decimal(38,2) DEFAULT NULL,
  `strategy_id` bigint(20) DEFAULT NULL,
  `ticker` varchar(255) DEFAULT NULL,
  `volume` bigint(20) DEFAULT NULL,
  `company_profile_id` bigint(20) DEFAULT NULL,
  `last_update` datetime(6) DEFAULT NULL,
  `valoration_ia` text DEFAULT NULL,
  `atr14` decimal(19,4) DEFAULT NULL,
  `bb_lower20` decimal(19,4) DEFAULT NULL,
  `bb_upper20` decimal(19,4) DEFAULT NULL,
  `ema12` decimal(19,4) DEFAULT NULL,
  `ema20` decimal(19,4) DEFAULT NULL,
  `ema200` decimal(19,4) DEFAULT NULL,
  `ema26` decimal(19,4) DEFAULT NULL,
  `ema50` decimal(19,4) DEFAULT NULL,
  `ema9` decimal(19,4) DEFAULT NULL,
  `macd_hist` decimal(19,4) DEFAULT NULL,
  `macd_line` decimal(19,4) DEFAULT NULL,
  `macd_signal` decimal(19,4) DEFAULT NULL,
  `rsi14` decimal(19,4) DEFAULT NULL,
  `rsi30` decimal(19,4) DEFAULT NULL,
  `origin` enum('ANALYSIS','SUGGESTION_SNAPSHOT') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh8hvv314y1i49uhfuo9eywirl` (`company_profile_id`),
  KEY `stocks_strategies_FK` (`strategy_id`),
  CONSTRAINT `FKh8hvv314y1i49uhfuo9eywirl` FOREIGN KEY (`company_profile_id`) REFERENCES `company_profile` (`id`),
  CONSTRAINT `stocks_strategies_FK` FOREIGN KEY (`strategy_id`) REFERENCES `strategies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1501 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `strategies`
--

DROP TABLE IF EXISTS `strategies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `strategies` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `objective_description` varchar(500) DEFAULT NULL,
  `objective_capital_to_risk` decimal(19,4) DEFAULT NULL,
  `objective_stop_loss_type` varchar(255) DEFAULT NULL,
  `objective_stop_loss_value` decimal(19,4) DEFAULT NULL,
  `objective_target_type` varchar(255) DEFAULT NULL,
  `objective_target_value` decimal(19,4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `strategy_evaluations`
--

DROP TABLE IF EXISTS `strategy_evaluations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `strategy_evaluations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `compliance_rate` decimal(5,2) NOT NULL,
  `compliant` bit(1) NOT NULL,
  `evaluated_at` datetime(6) NOT NULL,
  `latest` bit(1) NOT NULL,
  `price_at_evaluation` decimal(19,2) DEFAULT NULL,
  `summary` varchar(2000) DEFAULT NULL,
  `stock_id` bigint(20) NOT NULL,
  `strategy_name` varchar(255) DEFAULT NULL,
  `risk_reward_ratio` decimal(19,4) DEFAULT NULL,
  `stop_loss_price` decimal(19,4) DEFAULT NULL,
  `target_price` decimal(19,4) DEFAULT NULL,
  `recommended_shares` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs92jjn3wmhkclamvt8tu7q5xh` (`stock_id`),
  CONSTRAINT `FK84tu86wm620yammks85e59ipw` FOREIGN KEY (`stock_id`) REFERENCES `stocks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1483 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `suggested_ticker_snapshots`
--

DROP TABLE IF EXISTS `suggested_ticker_snapshots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `suggested_ticker_snapshots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deterministic_metrics` varchar(4000) DEFAULT NULL,
  `strategy_id` bigint(20) DEFAULT NULL,
  `suggested_at` datetime(6) DEFAULT NULL,
  `suitability_status` varchar(20) NOT NULL,
  `ticker` varchar(20) NOT NULL,
  `traceability` varchar(4000) DEFAULT NULL,
  `snapshot_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `suggested_ticker_snapshots_strategies_FK` (`strategy_id`),
  KEY `FK1cpafw2j03qrlnbi8jo1gf6vu` (`snapshot_id`),
  CONSTRAINT `FK1cpafw2j03qrlnbi8jo1gf6vu` FOREIGN KEY (`snapshot_id`) REFERENCES `suggestion_snapshots` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `suggested_ticker_snapshots_strategies_FK` FOREIGN KEY (`strategy_id`) REFERENCES `strategies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=709 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `suggestion_snapshots`
--

DROP TABLE IF EXISTS `suggestion_snapshots`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `suggestion_snapshots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `applied_filters` varchar(1000) DEFAULT NULL,
  `strategy_id` bigint(20) NOT NULL,
  `suggested_at` datetime(6) NOT NULL,
  `unmappable_rules` varchar(4000) DEFAULT NULL,
  `warnings` varchar(4000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `suggestion_snapshots_strategies_FK` (`strategy_id`),
  CONSTRAINT `suggestion_snapshots_strategies_FK` FOREIGN KEY (`strategy_id`) REFERENCES `strategies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;
--
-- Dumping routines for database 'marketanalysisdb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-09 17:25:16
