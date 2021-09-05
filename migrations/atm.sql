-- --------------------------------------------------------
-- Хост:                         127.0.0.1
-- Версия сервера:               8.0.26 - MySQL Community Server - GPL
-- Операционная система:         Win64
-- HeidiSQL Версия:              11.3.0.6295
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Дамп структуры базы данных atm
CREATE DATABASE IF NOT EXISTS `atm` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `atm`;

-- Дамп структуры для таблица atm.accounts
CREATE TABLE IF NOT EXISTS `accounts` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `client_id` bigint NOT NULL,
  `open_date` bigint NOT NULL,
  `account_status` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `balance` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;

-- Дамп данных таблицы atm.accounts: ~3 rows (приблизительно)
/*!40000 ALTER TABLE `accounts` DISABLE KEYS */;
INSERT IGNORE INTO `accounts` (`id`, `client_id`, `open_date`, `account_status`, `balance`) VALUES
	(1, 1, 1629021758, 'open', '10700'),
	(2, 2, 1439632958, 'open', '500000.5'),
	(3, 4, 1435505050, 'open', '20000000');
/*!40000 ALTER TABLE `accounts` ENABLE KEYS */;

-- Дамп структуры для таблица atm.clients
CREATE TABLE IF NOT EXISTS `clients` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `soname` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;

-- Дамп данных таблицы atm.clients: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `clients` DISABLE KEYS */;
INSERT IGNORE INTO `clients` (`id`, `name`, `soname`) VALUES
	(1, 'Василий', 'Пупкин'),
	(2, 'Сидоров', 'Сидор'),
	(3, 'Екатерина', 'Пупкина'),
	(4, 'Михаил', 'Кокляев');
/*!40000 ALTER TABLE `clients` ENABLE KEYS */;

-- Дамп структуры для таблица atm.companies
CREATE TABLE IF NOT EXISTS `companies` (
  `id` int DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `account` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Дамп данных таблицы atm.companies: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `companies` DISABLE KEYS */;
/*!40000 ALTER TABLE `companies` ENABLE KEYS */;

-- Дамп структуры для таблица atm.plastic_cards
CREATE TABLE IF NOT EXISTS `plastic_cards` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `account_id` int DEFAULT NULL,
  `client_id` int DEFAULT NULL,
  `pin` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `account_number` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `expiration_date` bigint unsigned DEFAULT NULL,
  `card_status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `card_check` (`pin`,`account_number`,`name`,`expiration_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;

-- Дамп данных таблицы atm.plastic_cards: ~3 rows (приблизительно)
/*!40000 ALTER TABLE `plastic_cards` DISABLE KEYS */;
INSERT IGNORE INTO `plastic_cards` (`id`, `account_id`, `client_id`, `pin`, `account_number`, `name`, `expiration_date`, `card_status`) VALUES
	(1, 1, 1, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', '0000111122223333', 'PUPKIN VASILIY', 1696104000, 'open'),
	(2, 2, 2, 'fe2592b42a727e977f055947385b709cc82b16b9a87f88c6abf3900d65d0cdc3', '1234123412341234', 'SIDOROV SIDOR', 1588276800, 'open'),
	(3, 3, 4, '38083c7ee9121e17401883566a148aa5c2e2d55dc53bc4a94a026517dbff3c6b', '1112111311141115', 'KOKLYAEV MICHAEL', 1675195200, 'close');
/*!40000 ALTER TABLE `plastic_cards` ENABLE KEYS */;

-- Дамп структуры для таблица atm.transactions
CREATE TABLE IF NOT EXISTS `transactions` (
  `id` int NOT NULL,
  `client_id` int DEFAULT NULL,
  `company_id` int DEFAULT NULL,
  `price` int DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `client_id_company_id_price` (`client_id`,`company_id`,`price`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

-- Дамп данных таблицы atm.transactions: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
