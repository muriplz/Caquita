package com.kryeit;

import org.jdbi.v3.core.Jdbi;

public class DatabaseUtils {
    public static void createTables() {
        Jdbi jdbi = Database.getJdbi();

        jdbi.withHandle(handle -> {
            // Create users table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    trust VARCHAR(255) NOT NULL
                )
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS currencies (
                    id BIGINT PRIMARY KEY,
                    level JSONB NOT NULL DEFAULT '{}',
                    beans INTEGER NOT NULL DEFAULT 0,
                    rolls INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (id) REFERENCES users(id)
                )
            """);

            // Create inventories table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS inventories (
                    id SERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    items JSONB NOT NULL DEFAULT '[]',
                    height INTEGER NOT NULL,
                    width INTEGER NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create landmarks table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS landmarks (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    position GEOMETRY(POINT, 4326) NOT NULL,
                    type VARCHAR(255) NOT NULL,
                    experience INTEGER NOT NULL DEFAULT 0
                )
            """);

            // Create trash_cans table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS cans (
                    id BIGINT PRIMARY KEY,
                    type VARCHAR(255) NOT NULL,
                    features JSONB NOT NULL DEFAULT '[]',
                    FOREIGN KEY (id) REFERENCES landmarks(id)
                )
            """);

            // Create spatial index for faster queries
            handle.execute("""
                CREATE INDEX IF NOT EXISTS landmarks_position_idx
                ON landmarks USING GIST(position)
            """);

            return null;
        });
    }

    public static void createForumTables() {
        Jdbi jdbi = Database.getJdbi();

        jdbi.withHandle(handle -> {
            // Create posts table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id SERIAL PRIMARY KEY,
                    landmark_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    content VARCHAR(255) NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (landmark_id) REFERENCES landmarks(id)
                )
            """);

            // Create comments table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS replies (
                    id SERIAL PRIMARY KEY,
                    message_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    content TEXT NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    FOREIGN KEY (message_id) REFERENCES messages(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS petitions (
                    id SERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    lat DOUBLE PRECISION NOT NULL,
                    lon DOUBLE PRECISION NOT NULL,
                    landmark_info JSONB NOT NULL,
                    accepted BOOLEAN NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    edition TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS petition_messages (
                    id SERIAL PRIMARY KEY,
                    petition_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    content VARCHAR(255) NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (petition_id) REFERENCES petitions(id)
                )
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS petition_replies (
                    id SERIAL PRIMARY KEY,
                    message_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    content TEXT NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                    FOREIGN KEY (message_id) REFERENCES messages(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            return null;
        });
    }
}
