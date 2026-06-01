CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `email` VARCHAR(100),
  `password` VARCHAR(255) NOT NULL,
  `custom_domain` VARCHAR(255),
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `short_url` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `short_code` VARCHAR(20) NOT NULL UNIQUE,
  `original_url` TEXT NOT NULL,
  `user_id` BIGINT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `expire_at` DATETIME,
  `click_count` BIGINT DEFAULT 0,
  INDEX `idx_short_code` (`short_code`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `click_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `short_code` VARCHAR(20) NOT NULL,
  `ip` VARCHAR(50),
  `user_agent` TEXT,
  `referer` TEXT,
  `browser` VARCHAR(50),
  `os` VARCHAR(50),
  `clicked_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_short_code` (`short_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
