CREATE DATABASE `web_log_data`;

CREATE TABLE `log_files` (
  `log_file_id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `log_file_name` varchar(100) NOT NULL,
  PRIMARY KEY (`log_file_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

CREATE TABLE `request_log` (
  `log_file_id` int(11) unsigned NOT NULL,
  `datetime` varchar(23) NOT NULL,
  `ip` char(15) NOT NULL,
  `request` varchar(45) NOT NULL,
  `status` varchar(3) NOT NULL,
  `user_agent` longtext NOT NULL,
  PRIMARY KEY (`log_file_id`,`datetime`,`ip`),
  CONSTRAINT `log_id_rl_fk` FOREIGN KEY (`log_file_id`) REFERENCES `log_files` (`log_file_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `blocked_ips` (
  `log_file_id` int(11) unsigned NOT NULL,
  `ip` char(15) NOT NULL,
  `limit_crossed` int(10) unsigned NOT NULL,
  `actual_no_of_requests` int(10) unsigned NOT NULL,
  `start_datetime` varchar(23) NOT NULL,
  `reason` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`ip`,`log_file_id`,`limit_crossed`,`actual_no_of_requests`,`start_datetime`),
  KEY `log_id_fk_idx` (`log_file_id`),
  CONSTRAINT `log_id_bi_fk` FOREIGN KEY (`log_file_id`) REFERENCES `log_files` (`log_file_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

