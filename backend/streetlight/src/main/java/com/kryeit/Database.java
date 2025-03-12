package com.kryeit;

import com.kryeit.auth.User;
import com.kryeit.auth.inventory.GridInventory;
import com.kryeit.auth.inventory.InventoryColumnMapper;
import com.kryeit.landmark.Landmark;
import com.kryeit.landmark.can.TrashCan;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.jackson2.Jackson2Plugin;


public class Database {
    private static final Jdbi JDBI;

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(Config.dbUser);
        hikariConfig.setPassword(Config.dbPassword);
        hikariConfig.setJdbcUrl(Config.dbUrl);

        JDBI = Jdbi.create(new HikariDataSource(hikariConfig));

        JDBI.registerRowMapper(ConstructorMapper.factory(User.class));
        JDBI.registerColumnMapper(GridInventory.class, new InventoryColumnMapper());

        // LANDMARKS
        JDBI.registerRowMapper(ConstructorMapper.factory(Landmark.class));

        JDBI.registerRowMapper(ConstructorMapper.factory(TrashCan.class));
        // LANDMARKS END

        JDBI.installPlugin(new Jackson2Plugin());
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}