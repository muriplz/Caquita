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
                    trust VARCHAR(255) NOT NULL,
                    experience INTEGER NOT NULL DEFAULT 0,
                    beans INTEGER NOT NULL DEFAULT 0
                )
            """);

            // Create inventories table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS inventories (
                    id SERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    width INTEGER NOT NULL,
                    height INTEGER NOT NULL,
                    item_placements JSONB NOT NULL DEFAULT '{}',
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create landmarks table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS landmarks (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    lat DOUBLE PRECISION NOT NULL,
                    lon DOUBLE PRECISION NOT NULL,
                    type VARCHAR(255) NOT NULL
                )
            """);

            // Create trash_cans table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS cans (
                    id SERIAL PRIMARY KEY,
                    type VARCHAR(255) NOT NULL,
                    features JSONB NOT NULL DEFAULT '[]'
                )
            """);

            return null;
        });
    }
}
