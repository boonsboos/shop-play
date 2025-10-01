SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `score_play`
--

DROP DATABASE IF EXISTS `score_play`;
CREATE DATABASE IF NOT EXISTS `score_play` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `score_play`;

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `friends`
--

CREATE TABLE `friends` (
    `user_id` int(11) NOT NULL,
    `friend_id` int(11) NOT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `games`
--

CREATE TABLE `games` (
    `game_id` int(11) NOT NULL,
    `scoring_method_id` int(11) NOT NULL,
    `name` varchar(150) NOT NULL,
    `description` mediumtext NOT NULL,
    `publisher` varchar(255) NOT NULL,
    `minimum_player_count` int(11) DEFAULT NULL,
    `maximum_player_count` int(11) DEFAULT NULL,
    `duration` int(11) DEFAULT NULL CHECK (`duration` > 0),
    `minimum_age` int(11) DEFAULT NULL,
    `release_date` date DEFAULT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `game_followers`
--

CREATE TABLE `game_followers` (
    `game_id` int(11) NOT NULL,
    `user_id` int(11) NOT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `game_pictures`
--

CREATE TABLE `game_pictures` (
    `game_id` int(11) NOT NULL,
    `picture_id` uuid NOT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `notifications`
--

CREATE TABLE `notifications` (
    `notification_id` uuid NOT NULL DEFAULT uuid_v7(),
    `user_id` int(11) NOT NULL,
    `content` text NOT NULL,
    `read` bit(1) NOT NULL DEFAULT b'0'
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `pictures`
--

CREATE TABLE `pictures` (
    `picture_id` uuid NOT NULL DEFAULT uuid_v7(),
    `picture_url` varchar(2048) NOT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `scores`
--

CREATE TABLE `scores` (
    `score_id` uuid NOT NULL DEFAULT uuid_v7(),
    `session_id` uuid NOT NULL,
    `session_player_id` uuid DEFAULT NULL,
    `game_id` int(11) NOT NULL,
    `score` decimal(10,2) NOT NULL,
    `turn` int(11) DEFAULT NULL,
    `achieved_on` date DEFAULT curdate()
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `scoring_methods`
--

CREATE TABLE `scoring_methods` (
    `scoring_method_id` int(11) NOT NULL,
    `name` text NOT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `sessions`
--

CREATE TABLE `sessions` (
    `session_id` uuid NOT NULL DEFAULT uuid_v7(),
    `game_id` int(11) NOT NULL,
    `host_user_id` int(11) NOT NULL,
    `start_time` timestamp NOT NULL,
    `end_time` timestamp NULL DEFAULT NULL,
    `end_of_session_picture_id` uuid DEFAULT NULL,
    `session_visibility` tinyint(4) NOT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `session_players`
--

CREATE TABLE `session_players` (
    `session_player_id` uuid NOT NULL DEFAULT uuid_v7(),
    `user_id` int(11) NOT NULL,
    `guest_name` varchar(255) DEFAULT NULL
);

-- --------------------------------------------------------

--
-- Tabelstructuur voor tabel `users`
--

CREATE TABLE `users` (
    `user_id` int(11) NOT NULL,
    `user_name` varchar(150) NOT NULL,
    `email` varchar(320) NOT NULL,
    `password_hash` varchar(120) NOT NULL,
    `profile_picture` uuid DEFAULT NULL
);

--
-- Indexen voor geëxporteerde tabellen
--

--
-- Indexen voor tabel `friends`
--
ALTER TABLE `friends`
    ADD KEY `FK_user_friends_friend_id` (`friend_id`),
    ADD KEY `FK_user_friends_user_id` (`user_id`);

--
-- Indexen voor tabel `games`
--
ALTER TABLE `games`
    ADD PRIMARY KEY (`game_id`),
    ADD KEY `FK_games_scoring_methods` (`scoring_method_id`);

--
-- Indexen voor tabel `game_followers`
--
ALTER TABLE `game_followers`
    ADD KEY `FK_gamefollowers_games` (`game_id`),
    ADD KEY `FK_gamefollowers_users` (`user_id`);

--
-- Indexen voor tabel `game_pictures`
--
ALTER TABLE `game_pictures`
    ADD KEY `FK_game_pictures_games` (`game_id`),
    ADD KEY `FK_game_pictures_pictures` (`picture_id`);

--
-- Indexen voor tabel `notifications`
--
ALTER TABLE `notifications`
    ADD PRIMARY KEY (`notification_id`),
    ADD KEY `FK_notifications_users` (`user_id`);

--
-- Indexen voor tabel `pictures`
--
ALTER TABLE `pictures`
    ADD PRIMARY KEY (`picture_id`);

--
-- Indexen voor tabel `scores`
--
ALTER TABLE `scores`
    ADD PRIMARY KEY (`score_id`),
    ADD KEY `FK_scores_sessionplayers` (`session_player_id`),
    ADD KEY `FK_scores_sessions` (`session_id`),
    ADD KEY `FK_scores_games` (`game_id`);

--
-- Indexen voor tabel `scoring_methods`
--
ALTER TABLE `scoring_methods`
    ADD PRIMARY KEY (`scoring_method_id`);

--
-- Indexen voor tabel `sessions`
--
ALTER TABLE `sessions`
    ADD PRIMARY KEY (`session_id`),
    ADD KEY `FK_sessions_users` (`host_user_id`),
    ADD KEY `FK_sessions_pictures` (`end_of_session_picture_id`);

--
-- Indexen voor tabel `session_players`
--
ALTER TABLE `session_players`
    ADD PRIMARY KEY (`session_player_id`),
    ADD KEY `FK_sessionplayers_users` (`user_id`);

--
-- Indexen voor tabel `users`
--
ALTER TABLE `users`
    ADD PRIMARY KEY (`user_id`),
    ADD KEY `FK_users_pictures` (`profile_picture`);

--
-- AUTO_INCREMENT voor geëxporteerde tabellen
--

--
-- AUTO_INCREMENT voor een tabel `games`
--
ALTER TABLE `games`
    MODIFY `game_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT voor een tabel `scoring_methods`
--
ALTER TABLE `scoring_methods`
    MODIFY `scoring_method_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT voor een tabel `users`
--
ALTER TABLE `users`
    MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Beperkingen voor geëxporteerde tabellen
--

--
-- Beperkingen voor tabel `friends`
--
ALTER TABLE `friends`
    ADD CONSTRAINT `FK_user_friends_friend_id` FOREIGN KEY (`friend_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `FK_user_friends_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Beperkingen voor tabel `games`
--
ALTER TABLE `games`
    ADD CONSTRAINT `FK_games_scoring_methods` FOREIGN KEY (`scoring_method_id`) REFERENCES `scoring_methods` (`scoring_method_id`);

--
-- Beperkingen voor tabel `game_followers`
--
ALTER TABLE `game_followers`
    ADD CONSTRAINT `FK_gamefollowers_games` FOREIGN KEY (`game_id`) REFERENCES `games` (`game_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `FK_gamefollowers_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Beperkingen voor tabel `game_pictures`
--
ALTER TABLE `game_pictures`
    ADD CONSTRAINT `FK_game_pictures_games` FOREIGN KEY (`game_id`) REFERENCES `games` (`game_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `FK_game_pictures_pictures` FOREIGN KEY (`picture_id`) REFERENCES `pictures` (`picture_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Beperkingen voor tabel `notifications`
--
ALTER TABLE `notifications`
    ADD CONSTRAINT `FK_notifications_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Beperkingen voor tabel `scores`
--
ALTER TABLE `scores`
    ADD CONSTRAINT `FK_scores_games` FOREIGN KEY (`game_id`) REFERENCES `games` (`game_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `FK_scores_sessionplayers` FOREIGN KEY (`session_player_id`) REFERENCES `session_players` (`session_player_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `FK_scores_sessions` FOREIGN KEY (`session_id`) REFERENCES `sessions` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Beperkingen voor tabel `sessions`
--
ALTER TABLE `sessions`
    ADD CONSTRAINT `FK_sessions_pictures` FOREIGN KEY (`end_of_session_picture_id`) REFERENCES `pictures` (`picture_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `FK_sessions_users` FOREIGN KEY (`host_user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Beperkingen voor tabel `session_players`
--
ALTER TABLE `session_players`
    ADD CONSTRAINT `FK_session_players_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Beperkingen voor tabel `users`
--
ALTER TABLE `users`
    ADD CONSTRAINT `FK_users_pictures` FOREIGN KEY (`profile_picture`) REFERENCES `pictures` (`picture_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
