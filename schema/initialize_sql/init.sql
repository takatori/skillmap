
CREATE SCHEMA IF NOT EXISTS skillmap DEFAULT CHARACTER SET utf8mb4;

USE skillmap;

CREATE TABLE IF NOT EXISTS users (
    PRIMARY KEY (user_id),
    user_id      INT UNSIGNED AUTO_INCREMENT                  NOT NULL,
    name         VARCHAR(20)                                  NOT NULL,

    UNIQUE INDEX(name)
) ENGINE=InnoDB DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_bin COMMENT='ユーザ';