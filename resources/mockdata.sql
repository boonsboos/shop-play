/* ===== 1) Scoring methods ===== */
INSERT INTO scoring_methods (name) VALUES
                                       ('Highest score wins'),
                                       ('Lowest score wins'),
                                       ('First to X score wins')
                                       ('Finishing on position Y wins')
                                       ('Last man standing wins');

/* ===== 2) Users (realistisch) ===== */
INSERT INTO users (user_name, email, password_hash, profile_picture) VALUES
                                                                         ('Sven Jansen',       'sven.jansen@example.nl',     '$2b$12$examplehashforSven1xxxxxxxxxxxxxxx', NULL),
                                                                         ('Lieke de Vries',    'lieke.de.vries@example.nl',  '$2b$12$examplehashforLieke2xxxxxxxxxxxx',   NULL),
                                                                         ('Ahmed El Idrissi',  'ahmed.elidrissi@example.nl', '$2b$12$examplehashforAhmed3xxxxxxxxxxx',   NULL);

/* ===== 3) Pictures (covers) ===== */
INSERT INTO pictures (picture_url) VALUES
                                       ('https://cdn.example.com/images/neon_drift.jpg'),
                                       ('https://cdn.example.com/images/tower_siege.jpg'),
                                       ('https://cdn.example.com/images/pixel_rally.jpg'),
                                       ('https://cdn.example.com/images/silent_run.jpg'),
                                       ('https://cdn.example.com/images/galactic_harvest.jpg');

/* ===== 4) Games (5 stuks) ===== */
INSERT INTO games (scoring_method_id, name, description, publisher, minimum_player_count, maximum_player_count, duration, minimum_age, release_date)
VALUES
    ((SELECT scoring_method_id FROM scoring_methods WHERE name='Highest score wins' LIMIT 1),
    'Neon Drift', 'Arcade racen met strakke leaderboards', 'ArcadeWorks', 1, 8, 15, 10, '2021-06-15'),
((SELECT scoring_method_id FROM scoring_methods WHERE name='Highest score wins' LIMIT 1),
 'Tower Siege', 'Tactische tower defense wedstrijden', 'HexaPlay', 2, 6, 40, 12, '2020-09-01'),
((SELECT scoring_method_id FROM scoring_methods WHERE name='Highest score wins' LIMIT 1),
 'Pixel Rally', 'Retro pixel-racer, korte heats', 'RetroLabs', 1, 4, 20, 6, '2019-03-10'),
((SELECT scoring_method_id FROM scoring_methods WHERE name='Lowest score wins' LIMIT 1),
 'Silent Run', 'Speedrun time trials (lager is beter)', 'SpeedSoft', 1, 1, 10, 8, '2022-11-05'),
((SELECT scoring_method_id FROM scoring_methods WHERE name='Highest score wins' LIMIT 1),
 'Galactic Harvest', 'Co-op punten sprokkelen in de ruimte', 'NebulaWorks', 2, 6, 30, 7, '2023-01-20');

/* Game-id’s vastleggen via namen (robust, ook bij bestaande data) */
SET @g1 := (SELECT game_id FROM games WHERE name='Neon Drift' LIMIT 1);
SET @g2 := (SELECT game_id FROM games WHERE name='Tower Siege' LIMIT 1);
SET @g3 := (SELECT game_id FROM games WHERE name='Pixel Rally' LIMIT 1);
SET @g4 := (SELECT game_id FROM games WHERE name='Silent Run' LIMIT 1);
SET @g5 := (SELECT game_id FROM games WHERE name='Galactic Harvest' LIMIT 1);

/* User-id’s vastleggen */
SET @u_sven  := (SELECT user_id FROM users WHERE user_name='Sven Jansen' LIMIT 1);
SET @u_lieke := (SELECT user_id FROM users WHERE user_name='Lieke de Vries' LIMIT 1);
SET @u_ahmed := (SELECT user_id FROM users WHERE user_name='Ahmed El Idrissi' LIMIT 1);

/* Picture-id’s vastleggen via url */
SET @p_nd := (SELECT picture_id FROM pictures WHERE picture_url='https://cdn.example.com/images/neon_drift.jpg' LIMIT 1);
SET @p_ts := (SELECT picture_id FROM pictures WHERE picture_url='https://cdn.example.com/images/tower_siege.jpg' LIMIT 1);
SET @p_pr := (SELECT picture_id FROM pictures WHERE picture_url='https://cdn.example.com/images/pixel_rally.jpg' LIMIT 1);
SET @p_sr := (SELECT picture_id FROM pictures WHERE picture_url='https://cdn.example.com/images/silent_run.jpg' LIMIT 1);
SET @p_gh := (SELECT picture_id FROM pictures WHERE picture_url='https://cdn.example.com/images/galactic_harvest.jpg' LIMIT 1);

/* ===== 5) Game → Pictures ===== */
INSERT INTO game_pictures (game_id, picture_id) VALUES
                                                    (@g1, @p_nd), (@g2, @p_ts), (@g3, @p_pr), (@g4, @p_sr), (@g5, @p_gh);

/* ===== 6) Sessions (2 per game: PUBLIC & ANON) — T1 tijdverloop ===== */
/* Maak vaste UUID’s per sessie zodat we ze kunnen refereren */
SET @s_g1_public := UUID(); SET @s_g1_anon := UUID();
SET @s_g2_public := UUID(); SET @s_g2_anon := UUID();
SET @s_g3_public := UUID(); SET @s_g3_anon := UUID();
SET @s_g4_public := UUID(); SET @s_g4_anon := UUID();
SET @s_g5_public := UUID(); SET @s_g5_anon := UUID();

/* Neon Drift */
INSERT INTO sessions (session_id, game_id, host_user_id, start_time, end_time, end_of_session_picture_id, session_visibility)
VALUES
    (@s_g1_public, @g1, @u_sven,  NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY, NULL, 2),
    (@s_g1_anon,   @g1, @u_lieke, NOW() - INTERVAL 2  DAY, NOW() - INTERVAL 1 DAY, NULL, 3);

/* Tower Siege */
INSERT INTO sessions (session_id, game_id, host_user_id, start_time, end_time, end_of_session_picture_id, session_visibility)
VALUES
    (@s_g2_public, @g2, @u_lieke, NOW() - INTERVAL 8  DAY, NOW() - INTERVAL 7 DAY, NULL, 2),
    (@s_g2_anon,   @g2, @u_ahmed, NOW() - INTERVAL 1  DAY, NOW() - INTERVAL 20 MINUTE, NULL, 3);

/* Pixel Rally */
INSERT INTO sessions (session_id, game_id, host_user_id, start_time, end_time, end_of_session_picture_id, session_visibility)
VALUES
    (@s_g3_public, @g3, @u_sven,  NOW() - INTERVAL 20 DAY, NOW() - INTERVAL 19 DAY, NULL, 2),
    (@s_g3_anon,   @g3, @u_sven,  NOW() - INTERVAL 3  DAY, NOW() - INTERVAL 2  DAY,  NULL, 3);

/* Silent Run */
INSERT INTO sessions (session_id, game_id, host_user_id, start_time, end_time, end_of_session_picture_id, session_visibility)
VALUES
    (@s_g4_public, @g4, @u_ahmed, NOW() - INTERVAL 5  DAY, NOW() - INTERVAL 4 DAY,  NULL, 2),
    (@s_g4_anon,   @g4, @u_lieke, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 30 MINUTE, NULL, 3);

/* Galactic Harvest */
INSERT INTO sessions (session_id, game_id, host_user_id, start_time, end_time, end_of_session_picture_id, session_visibility)
VALUES
    (@s_g5_public, @g5, @u_lieke, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY,  NULL, 2),
    (@s_g5_anon,   @g5, @u_sven,  NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 1 HOUR, NULL, 3);

/* ===== 7) Session Players ===== */
/* PUBLIC: echte users (2 per sessie) */
INSERT INTO session_players (session_player_id, user_id, guest_name) VALUES
(UUID(), @u_sven,  NULL),
(UUID(), @u_lieke, NULL),
(UUID(), @u_ahmed, NULL),
(UUID(), @u_sven,  'Bert');

/* ANON: gastspelers (G1A → per sessie opnieuw: “Gast speler 1/2”) */
INSERT INTO session_players (session_player_id, user_id, guest_name) VALUES
(UUID(), @u_sven, 'Gast speler 1'),
(UUID(), @u_sven, 'Gast speler 2'),
(UUID(), @u_ahmed, 'Gast speler 1'),
(UUID(), @u_ahmed, 'Gast speler 2'),
(UUID(), @u_lieke, 'Gast speler 1'),
(UUID(), @u_lieke, 'Gast speler 2');


/* ===== 8) Scores (S3: per game-type realistisch) ===== */
/* Helper: per sessie scores voor alle session_players daarin.
   PUBLIC sessies: koppel via users; ANON: via guest_name. */

/* Neon Drift — arcade (2000–15000), public */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score, turn)
SELECT UUID(), @s_g1_public, sp.session_player_id, @g1,
       CASE WHEN u.user_name='Sven Jansen' THEN 8421.50
            WHEN u.user_name='Lieke de Vries' THEN 5210.75
            ELSE 3000.00 END,
       1
FROM session_players sp
JOIN users u ON sp.user_id = u.user_id;

/* Neon Drift — anon */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g1_anon, sp.session_player_id, @g1,
       CASE WHEN sp.guest_name='Gast speler 1' THEN 3300.00 ELSE 2800.25 END
FROM session_players sp;

/* Tower Siege — strategy (8000–20000), public */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g2_public, sp.session_player_id, @g2,
       CASE WHEN u.user_name='Lieke de Vries'   THEN 14500.00
            WHEN u.user_name='Ahmed El Idrissi' THEN 13200.25
            ELSE 9000.00 END
FROM session_players sp
 JOIN users u ON sp.user_id = u.user_id;

/* Tower Siege — anon */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g2_anon, sp.session_player_id, @g2,
       CASE WHEN sp.guest_name='Gast speler 1' THEN 8900.00 ELSE 7500.50 END
FROM session_players sp;

/* Pixel Rally — retro (1000–6000), public */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g3_public, sp.session_player_id, @g3,
       CASE WHEN u.user_name='Sven Jansen'       THEN 4120.00
            WHEN u.user_name='Ahmed El Idrissi'  THEN 3999.50
            ELSE 1200.00 END
FROM session_players sp
JOIN users u ON sp.user_id = u.user_id;

/* Pixel Rally — anon */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g3_anon, sp.session_player_id, @g3,
       CASE WHEN sp.guest_name='Gast speler 1' THEN 2080.00 ELSE 1750.75 END
FROM session_players sp;

/* Silent Run — speedrun (laag is beter: ~50–200), public */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g4_public, sp.session_player_id, @g4,
       CASE WHEN u.user_name='Ahmed El Idrissi' THEN 58.32
            WHEN u.user_name='Sven Jansen'      THEN 62.10
            ELSE 150.00 END
FROM session_players sp
JOIN users u ON sp.user_id = u.user_id;

/* Silent Run — anon (laag) */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g4_anon, sp.session_player_id, @g4,
       CASE WHEN sp.guest_name='Gast speler 1' THEN 71.50 ELSE 78.90 END
FROM session_players sp;

/* Galactic Harvest — co-op (300–1000), public */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g5_public, sp.session_player_id, @g5,
       CASE WHEN u.user_name='Lieke de Vries' THEN 540.00
            WHEN u.user_name='Sven Jansen'    THEN 480.25
            ELSE 350.00 END
FROM session_players sp
JOIN users u ON sp.user_id = u.user_id;

/* Galactic Harvest — anon */
INSERT INTO scores (score_id, session_id, session_player_id, game_id, score)
SELECT UUID(), @s_g5_anon, sp.session_player_id, @g5,
       CASE WHEN sp.guest_name='Gast speler 1' THEN 330.00 ELSE 275.75 END
FROM session_players sp;

/* ===== 9) Friends (wederzijds) ===== */
INSERT INTO friends (user_id, friend_id) VALUES
(@u_sven, @u_lieke), (@u_lieke, @u_sven),
(@u_sven, @u_ahmed), (@u_ahmed, @u_sven),
(@u_lieke, @u_ahmed), (@u_ahmed, @u_lieke);

/* ===== 10) Game followers ===== */
INSERT INTO game_followers (game_id, user_id) VALUES
(@g1, @u_sven), (@g1, @u_lieke),
(@g2, @u_lieke),
(@g3, @u_sven), (@g3, @u_ahmed),
(@g4, @u_ahmed),
(@g5, @u_sven), (@g5, @u_lieke);

/* ===== 11) Notifications (mix read/unread, T1 tijden) ===== */
INSERT INTO notifications (user_id, content, `read`)
VALUES
    (@u_sven,  'Je hebt een nieuwe highscore op Neon Drift!',                b'0'),
    (@u_lieke, 'Je volgt nu Tower Siege.',                                   b'1'),
    (@u_ahmed, 'Uitnodiging: speel Pixel Rally vanavond met Sven.',          b'0');
