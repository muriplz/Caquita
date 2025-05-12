package com.kryeit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryeit.auth.Friendship;
import com.kryeit.auth.User;
import com.kryeit.auth.avatar.UnlockedAvatar;
import com.kryeit.auth.currency.Currencies;
import com.kryeit.auth.inventory.Inventory;
import com.kryeit.landmark.forum.Message;
import com.kryeit.landmark.forum.Reply;
import com.kryeit.landmark.forum.petitions.Petition;
import com.kryeit.landmark.forum.petitions.PetitionMessage;
import com.kryeit.landmark.forum.petitions.PetitionReply;
import com.kryeit.landmark.plastic_container.PlasticContainer;
import com.kryeit.landmark.trash_can.TrashCan;
import com.kryeit.spawning.SpawningItem;
import com.kryeit.stats.GlobalStats;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.jackson2.Jackson2Plugin;


public class Database {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Jdbi JDBI;

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(Config.dbUser);
        hikariConfig.setPassword(Config.dbPassword);
        hikariConfig.setJdbcUrl(Config.dbUrl);

        JDBI = Jdbi.create(new HikariDataSource(hikariConfig));

        JDBI.registerRowMapper(ConstructorMapper.factory(User.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Currencies.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Inventory.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(UnlockedAvatar.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Friendship.class));
        
        JDBI.registerRowMapper(ConstructorMapper.factory(SpawningItem.class));

        // LANDMARKS
        JDBI.registerRowMapper(ConstructorMapper.factory(TrashCan.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(PlasticContainer.class));
        // LANDMARKS END

        // FORUMS
        JDBI.registerRowMapper(ConstructorMapper.factory(Message.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Reply.class));

        JDBI.registerRowMapper(ConstructorMapper.factory(PetitionMessage.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(PetitionReply.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Petition.class));
        // FORUMS END

        JDBI.registerRowMapper(ConstructorMapper.factory(GlobalStats.class));

        JDBI.installPlugin(new Jackson2Plugin());
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}