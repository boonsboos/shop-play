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
-- Create Tabels
------------------------------------------------
-- game
CREATE TABLE `game`
(
    `game_id` int NOT NULL,
    `scoring_method_id` int NOT NULL,
    `name` varchar(150) NOT NULL,
    `description` mediumtext NOT NULL,
    `publisher` varchar(255) NOT NULL,
    `minimum_player_count` int DEFAULT NULL,
    `maximum_player_count` int DEFAULT NULL,
    `duration` int(11) DEFAULT NULL CHECK (`duration` > 0),
    `minimum_age` int  DEFAULT NULL,
    `release_date` date DEFAULT NULL CUREDATE()
);

-- scoring_method
CREATE TABLE `scoring_method`
(
    `scoring_method_id` int NOT NULL,
    `name` text NOT NULL -- klopt dit?
);

-- game_pictures
CREATE TABLE `game_pictures`
(
    `game_id` int NOT NULL,
    `picture_id` UUID NOT NULL -- klopt dit?
);

-- game_followers
CREATE TABLE `game_followers`
(
    `game_id` int NOT NULL,
    `user_id` int NOT NULL,
);

-- session
CREATE TABLE `session`
(
    `session_id` UUID NOT NULL, -- klopt dit?
    `game_id` int NOT NULL,
    `host_user_id` int NOT NULL,
    `start_time` timestamp NOT NULL,
    `end_time` timestamp NULL DEFAULT NULL,
    `end_of_session_picture_id` UUID NOT NULL, -- klopt dit?
    `session_visibility` tinyint NOT NULL
);

-- scores
CREATE TABLE `scores`
(
    `score_id` UUID NOT NULL, -- klopt dit?
    `session_id` UUID NOT NULL, -- klopt dit?
    `session_player_id` UUID NOT NULL, -- klopt dit?
    `game_id` int(11) NOT NULL,
    `score` decimal(10, 2) NOT NULL,
    `turn` int DEFAULT NULL,
    `achieved_on` date DEFAULT
);

-- user
CREATE TABLE `user` (
    `user_id` int NOT NULL,
    `user_name` varchar(150) NOT NULL,
    `email` varchar(320) NOT NULL,
    `passwordHash` varchar(120) NOT NULL,
    `profilePicture` varchar(200) DEFAULT NULL
);

-- pictures
CREATE TABLE `pictures`
(
    `picture_id` UUID NOT NULL, -- klopt dit?
    `picture_url` varchar(2048) NOT NULL
);

-- session_player
CREATE TABLE `session_player`
(
    `session_player_id` UUID NOT NULL, -- klopt dit?
    `user_id` int NOT NULL,
    `guest_name` varchar(255) DEFAULT NULL
);

-- friends
CREATE TABLE `friends`
(
    `user_id` int NOT NULL,
    `friend_id` int NOT NULL,
    `friend_status` int NOT NULL
);

-- notifications
CREATE TABLE `notifications`
(
    `notifications_id` UUID NOT NULL, -- klopt dit?
    `user_id` int NOT NULL,
    `content` text NOT NULL,
    `Read` BIT(1) NOT NULL DEFAULT b'0',
);