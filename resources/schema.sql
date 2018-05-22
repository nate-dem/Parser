
CREATE DATABASE IF NOT EXISTS `parser`;

CREATE TABLE `parser`.`request_log` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `date_time` timestamp(3) NOT NULL,
  `ip_address` INT UNSIGNED NOT NULL,
  `request_method` VARCHAR(20) NOT NULL,
  `status` SMALLINT NOT NULL,
  `user_agent` VARCHAR(255) NOT NULL,
  UNIQUE (`date_time`, `ip_address`),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `ip_address` (`ip_address` ASC)
);
 
CREATE TABLE `parser`.`request_search_log` (
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
);
