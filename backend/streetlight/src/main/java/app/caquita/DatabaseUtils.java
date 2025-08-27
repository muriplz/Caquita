package app.caquita;

import app.caquita.stats.GlobalStats;
import org.jdbi.v3.core.Jdbi;

public class DatabaseUtils {
    public static void createTables() {
        Jdbi jdbi = Database.getJdbi();

        jdbi.withHandle(handle -> {
            // PostGIS extension
            handle.execute("CREATE EXTENSION IF NOT EXISTS postgis");

            // Create users table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                    connection TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                    trust VARCHAR(255) NOT NULL DEFAULT 'DEFAULT',
                    avatar VARCHAR(255) NOT NULL DEFAULT 'trash_can'
                )
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS last_locations (
                    id BIGINT PRIMARY KEY,
                    position GEOMETRY(POINT, 4326) NOT NULL,
                    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                    device VARCHAR(255),
                    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

            handle.execute("""
                CREATE INDEX IF NOT EXISTS last_locations_position_idx
                ON last_locations USING GIST(position)
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS unlocked_avatars (
                    id SERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    avatar VARCHAR(255) NOT NULL,
                    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    UNIQUE (user_id, avatar)
                )
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS friendships (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    friend_id BIGINT NOT NULL,
                    status VARCHAR(10) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED', 'REJECTED')),
                    creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    edition TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (user_id, friend_id),
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (friend_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);


            handle.execute("""
                CREATE TABLE IF NOT EXISTS currencies (
                    id BIGINT PRIMARY KEY,
                    experience INTEGER NOT NULL DEFAULT 0,
                    beans INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (id) REFERENCES users(id)
                )
            """);

            // Create inventories table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS inventories (
                    id SERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    items JSONB NOT NULL DEFAULT '[]',
                    width INTEGER NOT NULL,
                    height INTEGER NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create trash_cans table
            handle.execute("""
                CREATE TABLE IF NOT EXISTS trash_cans (
                    id BIGSERIAL PRIMARY KEY,
                    position GEOMETRY(POINT, 4326) NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    author BIGINT NOT NULL,
                    broken BOOLEAN NOT NULL DEFAULT FALSE,
                    ashtray BOOLEAN NOT NULL DEFAULT FALSE,
                    windblown BOOLEAN NOT NULL DEFAULT FALSE,
                    flooded BOOLEAN NOT NULL DEFAULT FALSE,
                    overwhelmed BOOLEAN NOT NULL DEFAULT FALSE,
                    poopbags BOOLEAN NOT NULL DEFAULT FALSE,
                    art BOOLEAN NOT NULL DEFAULT FALSE
                )
            """);

            //handle.execute("""
            //    CREATE TABLE IF NOT EXISTS plastic_bins (
            //        id BIGSERIAL PRIMARY KEY,
            //        position GEOMETRY(POINT, 4326) NOT NULL,
            //        name VARCHAR(255) NOT NULL,
            //        description TEXT NOT NULL,
            //        author BIGINT NOT NULL,
            //        broken BOOLEAN NOT NULL DEFAULT FALSE,
            //        underground BOOLEAN NOT NULL DEFAULT FALSE,
            //        bottlenecked BOOLEAN NOT NULL DEFAULT FALSE,
            //        modern BOOLEAN NOT NULL DEFAULT FALSE,
            //        overwhelmed BOOLEAN NOT NULL DEFAULT FALSE
            //    )
            //""");

            // Create spatial index for faster queries
            handle.execute("""
                CREATE INDEX IF NOT EXISTS trash_cans_position_idx
                ON trash_cans USING GIST(position)
            """);
            //handle.execute("""
            //    CREATE INDEX IF NOT EXISTS plastic_bins_position_idx
            //    ON plastic_containers USING GIST(position)
            //""");
            //handle.execute("""
            //    CREATE INDEX IF NOT EXISTS spawning_items_position_idx
            //    ON spawning_items USING GIST(position)
            //""");
            //handle.execute("""
            //    CREATE INDEX IF NOT EXISTS spawning_items_duration_idx
            //    ON spawning_items (duration)
            //""");

            handle.execute("""
                CREATE TABLE IF NOT EXISTS global_stats (
                    stat VARCHAR(255) PRIMARY KEY,
                    value BIGINT NOT NULL DEFAULT 0
                )
            """);

            for (GlobalStats.GlobalStat stat : GlobalStats.GlobalStat.values()) {
                handle.execute("""
                    INSERT INTO global_stats (stat, value)
                    VALUES (?, 0)
                    ON CONFLICT (stat) DO NOTHING
                """, stat.name());
            }

            return null;
        });

        createForumTables();
    }

    public static void createForumTables() {
        Jdbi jdbi = Database.getJdbi();

        jdbi.withHandle(handle -> {
            //handle.execute("""
            //    CREATE TABLE IF NOT EXISTS messages (
            //        id SERIAL PRIMARY KEY,
            //        landmark_id BIGINT NOT NULL,
            //        user_id BIGINT NOT NULL,
            //        content VARCHAR(255) NOT NULL,
            //        creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
            //        FOREIGN KEY (user_id) REFERENCES users(id),
            //        FOREIGN KEY (landmark_id) REFERENCES landmarks(id)
            //    )
            //""");

            //handle.execute("""
            //    CREATE TABLE IF NOT EXISTS replies (
            //        id SERIAL PRIMARY KEY,
            //        message_id BIGINT NOT NULL,
            //        user_id BIGINT NOT NULL,
            //        content TEXT NOT NULL,
            //        creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
            //        FOREIGN KEY (message_id) REFERENCES messages(id),
            //        FOREIGN KEY (user_id) REFERENCES users(id)
            //    )
            //""");

            handle.execute("""
                CREATE TABLE IF NOT EXISTS petitions (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(15) NOT NULL,
                    description TEXT NOT NULL,
                    user_id BIGINT NOT NULL,
                    type VARCHAR(255) NOT NULL,
                    position GEOMETRY(POINT, 4326) NOT NULL,
                    info JSONB NOT NULL,
                    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                    edition TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create spatial index for faster queries
            handle.execute("""
                CREATE INDEX IF NOT EXISTS petitions_position_idx
                ON petitions USING GIST(position)
            """);

            handle.execute("""
                CREATE TABLE IF NOT EXISTS petition_messages (
                    id SERIAL PRIMARY KEY,
                    petition_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    content VARCHAR(255) NOT NULL,
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
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
                    creation TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                    FOREIGN KEY (message_id) REFERENCES petition_messages(id),
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);


            return null;
        });
    }
}
