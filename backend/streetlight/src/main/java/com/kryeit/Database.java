package com.kryeit;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.kryeit.auth.User;
import com.kryeit.auth.inventory.Inventory;
import com.kryeit.landmark.Landmark;
import com.kryeit.landmark.can.TrashCan;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.jackson2.Jackson2Plugin;

import java.util.Optional;


public class Database {
    private static final Jdbi JDBI;

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(Config.dbUser);
        hikariConfig.setPassword(Config.dbPassword);
        hikariConfig.setJdbcUrl(Config.dbUrl);

        JDBI = Jdbi.create(new HikariDataSource(hikariConfig));

        JDBI.registerRowMapper(ConstructorMapper.factory(User.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Inventory.class));

        // LANDMARKS
        JDBI.registerRowMapper(ConstructorMapper.factory(Landmark.class));

        JDBI.registerRowMapper(ConstructorMapper.factory(TrashCan.class));
        // LANDMARKS END

        jsonMappers();
        JDBI.installPlugin(new Jackson2Plugin());
    }

    private static void jsonMappers() {
        JDBI.registerColumnMapper(JsonArray.class, (r, idx, ctx) -> {
            String json = r.getString(idx);
            if (json == null) return null;
            return JsonParser.parseString(json).getAsJsonArray();
        });

        JDBI.registerArgument((ArgumentFactory) (type, value, config) -> {
            if (value instanceof JsonArray) {
                return Optional.of((position, statement, ctx) -> statement.setString(position, value.toString()));
            }
            return Optional.empty();
        });
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}