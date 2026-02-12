CREATE DATABASE IF NOT EXISTS insidemovie_backend
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS insidemovie_ai
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE insidemovie_ai;

CREATE TABLE IF NOT EXISTS movie_emotion_summary (
  movie_id INT PRIMARY KEY,
  joy FLOAT NULL,
  anger FLOAT NULL,
  fear FLOAT NULL,
  disgust FLOAT NULL,
  sadness FLOAT NULL
);
