
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `app_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `username` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','USER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','DISABLED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKspsnwr241e9k9c8p5xl4k45ih` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `document_parse_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `document_parse_tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auto_start_import` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `failed_reason` text COLLATE utf8mb4_unicode_ci,
  `finished_at` datetime(6) DEFAULT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` enum('FAILED','PENDING','RUNNING','SUCCEEDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `upload_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `document_uploads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `document_uploads` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `document_count` int NOT NULL,
  `file_size` bigint NOT NULL,
  `ignored_file_count` int NOT NULL,
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parse_status` enum('FAILED','PARSED','PARSING','QUEUED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `skipped_file_count` int NOT NULL,
  `skipped_files_json` text COLLATE utf8mb4_unicode_ci,
  `stored_path` varchar(600) COLLATE utf8mb4_unicode_ci NOT NULL,
  `upload_sha256` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `upload_type` enum('MARKDOWN','ZIP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `uploaded_by_email` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `uploaded_by_id` bigint NOT NULL,
  `uploaded_by_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `favorite_questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite_questions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `question_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_question_user_question` (`user_id`,`question_id`),
  KEY `FKhniwo1he5ox422emcof9o3h50` (`question_id`),
  CONSTRAINT `FK60qh1tmfb0lcq4wthkcnj9okb` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`),
  CONSTRAINT `FKhniwo1he5ox422emcof9o3h50` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `import_job_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_job_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `import_job_id` bigint NOT NULL,
  `level` enum('ERROR','INFO','WARN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `payload` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `import_jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_jobs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codex_session_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `document_id` bigint NOT NULL,
  `document_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `failed_reason` text COLLATE utf8mb4_unicode_ci,
  `finished_at` datetime(6) DEFAULT NULL,
  `generated_question_count` int NOT NULL,
  `started_at` datetime(6) DEFAULT NULL,
  `status` enum('CANCELLED','FAILED','PENDING','RUNNING','SUCCEEDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `knowledge_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_documents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `archive_entry_path` varchar(600) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `archive_original_filename` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content_sha256` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `file_size` bigint NOT NULL,
  `original_filename` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_type` enum('MARKDOWN','ZIP') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('FAILED','PROCESSED','PROCESSING','UPLOADED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `stored_path` varchar(600) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `upload_id` bigint NOT NULL,
  `uploaded_by_email` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `uploaded_by_id` bigint NOT NULL,
  `uploaded_by_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `practice_answer_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `practice_answer_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `answered_at` datetime(6) NOT NULL,
  `correct` bit(1) NOT NULL,
  `practice_mode` enum('FAVORITE','RANDOM','SEARCH','SEQUENTIAL','TAG','WRONG') COLLATE utf8mb4_unicode_ci NOT NULL,
  `selected_option_key` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  `time_spent_seconds` int DEFAULT NULL,
  `question_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmkdfjfe3vycq0kc5qspwaktee` (`question_id`),
  KEY `FKqeevbiwmh98agggrqhrigpgkv` (`user_id`),
  CONSTRAINT `FKmkdfjfe3vycq0kc5qspwaktee` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`),
  CONSTRAINT `FKqeevbiwmh98agggrqhrigpgkv` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `question_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `correct` bit(1) NOT NULL,
  `option_key` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  `question_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_options_question_key` (`question_id`,`option_key`),
  CONSTRAINT `FKsb9v00wdrgc9qojtjkv7e1gkp` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `question_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_tags` (
  `question_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`question_id`,`tag_id`),
  KEY `FK4s4qdqgvc98lx55s3hu9vqam7` (`tag_id`),
  CONSTRAINT `FK4s4qdqgvc98lx55s3hu9vqam7` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`),
  CONSTRAINT `FKee6kn1hbh2ka2qj64bv30esbw` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `questions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `answer_analysis` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `codex_review_summary` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `difficulty` enum('EASY','HARD','MEDIUM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `knowledge_point` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_document_id` bigint NOT NULL,
  `source_import_job_id` bigint NOT NULL,
  `status` enum('ACTIVE','DISABLED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `stem` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `stem_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('SINGLE_CHOICE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_questions_import_job_stem_hash` (`source_import_job_id`,`stem_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `service_campus_net_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_campus_net_login` (
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `net_account` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `carrier` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `net_password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wlan_user_ip` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wlan_user_mac` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wlan_ac_ip` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `wlan_ac_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_del` tinyint DEFAULT '0',
  `run_status` tinyint DEFAULT '0',
  `refresh_time` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT '03-00',
  PRIMARY KEY (`email`),
  KEY `idx_service_campus_net_login_is_del` (`is_del`),
  KEY `idx_service_campus_net_login_run_status` (`run_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `service_dorm_electricity_alert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_dorm_electricity_alert` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `feeitemid` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `level` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `campus` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `campus_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `building` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `building_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `room` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `room_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `run_status` tinyint DEFAULT '0',
  `threshold` double DEFAULT NULL,
  `is_del` tinyint DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_service_dorm_electricity_alert_email` (`email`),
  KEY `idx_service_dorm_electricity_alert_run_status` (`run_status`,`is_del`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `service_dorm_electricity_alert_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_dorm_electricity_alert_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `feeitemid` int DEFAULT NULL,
  `type` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `level` int DEFAULT NULL,
  `campus` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `building` int DEFAULT NULL,
  `room` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `room_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_service_dorm_room_campus_building` (`campus`,`building`),
  KEY `idx_service_dorm_room_lookup` (`campus`,`building`,`room`)
) ENGINE=InnoDB AUTO_INCREMENT=6998 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `service_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `relation_table` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `operation_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operation_status` tinyint DEFAULT NULL,
  `remarks` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idx_service_log_email_relation_time` (`email`,`relation_table`,`create_time`),
  KEY `idx_service_log_relation_table` (`relation_table`)
) ENGINE=InnoDB AUTO_INCREMENT=3252 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nick_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `is_del` tinyint DEFAULT '0',
  PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tags` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` enum('DATABASE','DESIGN','FRAMEWORK','JAVA','MIDDLEWARE','NETWORK','OTHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `normalized_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `question_count` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tags_normalized_name` (`normalized_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wrong_question_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wrong_question_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `correct_after_wrong_count` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `last_answered_at` datetime(6) NOT NULL,
  `last_wrong_at` datetime(6) NOT NULL,
  `mastered` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `wrong_count` int NOT NULL,
  `question_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wrong_question_user_question` (`user_id`,`question_id`),
  KEY `FKb3y35q6r697bsas02tlygm95k` (`question_id`),
  CONSTRAINT `FKb3y35q6r697bsas02tlygm95k` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`),
  CONSTRAINT `FKs7n3h4x764n37rrr8ftv6bao5` FOREIGN KEY (`user_id`) REFERENCES `app_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

