SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

------------------------------------------------
-- Database: shop_play
------------------------------------------------
DROP DATABASE IF EXISTS `shop_play`;
CREATE DATABASE IF NOT EXISTS `shop_play` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `shop_play`;

------------------------------------------------
-- Create Tables
------------------------------------------------
-- games
CREATE TABLE `games`
(
    `game_id` int PRIMARY KEY AUTO_INCREMENT,
    `scoring_method_id` int NOT NULL,
    `name` varchar(150) NOT NULL,
    `description` mediumtext NOT NULL,
    `publisher` varchar(255) NOT NULL,
    `minimum_player_count` int DEFAULT NULL,
    `maximum_player_count` int DEFAULT NULL,
    `duration` int(11) DEFAULT NULL CHECK (`duration` > 0),
    `minimum_age` int  DEFAULT NULL,
    `release_date` date DEFAULT NULL
);

-- scoring_methods
CREATE TABLE `scoring_methods`
(
    `scoring_method_id` int PRIMARY KEY AUTO_INCREMENT,
    `name` text NOT NULL
);

-- game_pictures
CREATE TABLE `game_pictures`
(
    `game_id` int NOT NULL,
    `picture_id` UUID NOT NULL
);

-- game_followers
CREATE TABLE `game_followers`
(
    `game_id` int NOT NULL,
    `user_id` int NOT NULL
);

-- sessions
CREATE TABLE `sessions`
(
    `session_id` UUID PRIMARY KEY DEFAULT UUID_v7(),
    `game_id` int NOT NULL,
    `host_user_id` int NOT NULL,
    `start_time` timestamp NOT NULL,
    `end_time` timestamp NULL DEFAULT NULL,
    `end_of_session_picture_id` UUID NOT NULL,
    `session_visibility` tinyint NOT NULL
);

-- scores
CREATE TABLE `scores`
(
    `score_id` UUID PRIMARY KEY DEFAULT UUID_v7(),
    `session_id` UUID NOT NULL,
    `session_player_id` UUID NOT NULL,
    `game_id` int(11) NOT NULL,
    `score` decimal(10, 2) NOT NULL,
    `turn` int DEFAULT NULL,
    `achieved_on` date DEFAULT CURRENT_DATE()
);

-- users
CREATE TABLE `users` (
                         `user_id` int PRIMARY KEY AUTO_INCREMENT,
                         `user_name` varchar(150) NOT NULL,
                         `email` varchar(320) NOT NULL,
                         `password_hash` varchar(120) NOT NULL,
                         `profile_picture` varchar(200) DEFAULT NULL
);

-- pictures
CREATE TABLE `pictures`
(
    `picture_id` UUID PRIMARY KEY DEFAULT UUID_v7(),
    `picture_url` varchar(2048) NOT NULL
);

-- session_players
CREATE TABLE `session_players`
(
    `session_player_id` UUID PRIMARY KEY DEFAULT UUID_v7(),
    `user_id` int NOT NULL,
    `guest_name` varchar(255) DEFAULT NULL
);

-- friends
CREATE TABLE `friends`
(
    `user_id` int NOT NULL,
    `friend_id` int NOT NULL
);

-- notifications
CREATE TABLE `notifications`
(
    `notification_id` UUID PRIMARY KEY DEFAULT UUID_v7(),
    `user_id` int NOT NULL,
    `content` text NOT NULL,
    `read` BIT(1) NOT NULL DEFAULT b'0'
);