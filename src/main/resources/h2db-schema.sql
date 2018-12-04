DROP SCHEMA IF EXISTS PARSER;

CREATE SCHEMA IF NOT EXISTS PARSER;

CREATE TABLE IF NOT EXISTS `PARSER`.`LOG_ENTRIES` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `start_date` timestamp(3) NOT NULL,
  `ip_address` VARCHAR(20) NOT NULL,
  `request_method` VARCHAR(20) NOT NULL,
  `status` SMALLINT NOT NULL,
  `user_agent` VARCHAR(255) NOT NULL,
  UNIQUE (`start_date`, `ip_address`),
  PRIMARY KEY (`id`),
  
  INDEX `ip_address` (`ip_address` ASC)
);

CREATE TABLE IF NOT EXISTS `PARSER`.`BLOCKED_IPS` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `ip_address` VARCHAR(20) NOT NULL,
  `num_request` SMALLINT UNSIGNED NOT NULL,
  `reason` VARCHAR(255) NOT NULL,
   UNIQUE (`ip_address`),
   PRIMARY KEY (`id`),
   UNIQUE INDEX `id_UNIQUE` (`id` ASC),
   CONSTRAINT `fk_ip_address` FOREIGN KEY (`ip_address`) REFERENCES `PARSER`.`LOG_ENTRIES`(`ip_address`)
);

/*

UNIQUE INDEX `id_UNIQUE` (`id` ASC),

CREATE TABLE `Parser`.`request_search_log` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `date_time` timestamp(3) NOT NULL,
  `ip_address` INT UNSIGNED NOT NULL,
  `duration` VARCHAR(9) NOT NULL,
  `num_requests` SMALLINT UNSIGNED NOT NULL,
  `remark` VARCHAR(255) NOT NULL,
   UNIQUE (`date_time`, `ip_address`,`duration`),
   PRIMARY KEY (`id`),
   UNIQUE INDEX `id_UNIQUE` (`id` ASC),
   CONSTRAINT `fk_ip_address` FOREIGN KEY (`ip_address`) REFERENCES `parser`.`request_log`(`ip_address`)
); */
