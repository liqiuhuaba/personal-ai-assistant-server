CREATE DATABASE IF NOT EXISTS personal_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE personal_ai;

CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL UNIQUE,
  `password_hash` VARCHAR(256) NOT NULL,
  `avatar_url` VARCHAR(512),
  `cloud_sync` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `calendar_event` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(256) NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME,
  `remind_at` DATETIME,
  `source` ENUM('manual','ai') NOT NULL DEFAULT 'manual',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_start (`user_id`, `start_time`),
  INDEX idx_remind_at (`remind_at`),
  CONSTRAINT fk_ce_user FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `chat_session` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `mode` ENUM('chat','biography','learning') NOT NULL DEFAULT 'chat',
  `title` VARCHAR(256),
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_updated (`user_id`, `updated_at`),
  CONSTRAINT fk_cs_user FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `chat_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `role` ENUM('user','assistant') NOT NULL,
  `content` MEDIUMTEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session (`session_id`),
  CONSTRAINT fk_cm_session FOREIGN KEY (`session_id`) REFERENCES `chat_session`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `biography_event` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `event_date` VARCHAR(10),
  `title` VARCHAR(256) NOT NULL,
  `content` MEDIUMTEXT NOT NULL,
  `category` VARCHAR(64),
  `source_msg_id` BIGINT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_date (`user_id`, `event_date`),
  CONSTRAINT fk_be_user FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  CONSTRAINT fk_be_msg FOREIGN KEY (`source_msg_id`) REFERENCES `chat_message`(`id`) ON DELETE SET NULL,
  CONSTRAINT chk_event_date CHECK (`event_date` IS NULL OR `event_date` REGEXP '^[0-9]{4}(-[0-9]{2}(-[0-9]{2})?)?$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `learning_session` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `subject` VARCHAR(128) NOT NULL,
  `topic` VARCHAR(256),
  `score` INT CHECK (`score` IS NULL OR `score` BETWEEN 0 AND 100),
  `duration_min` INT CHECK (`duration_min` IS NULL OR `duration_min` >= 0),
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (`user_id`),
  CONSTRAINT fk_ls_user FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `search_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `query` VARCHAR(512) NOT NULL,
  `summary` TEXT,
  `sources` JSON,
  `starred` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (`user_id`),
  INDEX idx_user_starred (`user_id`, `starred`, `created_at`),
  CONSTRAINT fk_sh_user FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
