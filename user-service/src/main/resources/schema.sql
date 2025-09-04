CREATE TABLE IF NOT EXISTS `user`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `username`   VARCHAR(50)  NOT NULL,
    `email`      VARCHAR(100) NOT NULL,
    `password`   VARCHAR(100) NOT NULL,
    `role`       VARCHAR(20)  NOT NULL DEFAULT 'USER',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `deleted`    INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE (`username`),
    UNIQUE (`email`)
);
