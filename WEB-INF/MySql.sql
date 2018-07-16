DROP TABLE IF EXISTS `product`;

CREATE TABLE `product`
(
	`id` CHAR(16) NOT NULL,
	`code` VARCHAR(100) COMMENT 'Code',
	`name` VARCHAR(100) COMMENT 'Name',
	`created_user_id` CHAR(16),
	`updated_user_id` CHAR(16),
	`deleted_user_id` CHAR(16),
	`created_at` DATETIME,
	`updated_at` DATETIME,
	`deleted_at` DATETIME,
	`deleted` TINYINT(1),
	PRIMARY KEY (`id`)
);

ALTER TABLE `product` COMMENT 'Product';
