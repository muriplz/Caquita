package app.caquita.storage;

import app.caquita.Config;
import app.caquita.auth.Friendship;
import app.caquita.auth.User;
import app.caquita.auth.avatar.UnlockedAvatar;
import app.caquita.auth.inventory.tools.Tool;
import app.caquita.landmark.forum.petitions.Petition;
import app.caquita.landmark.forum.petitions.PetitionMessage;
import app.caquita.landmark.forum.petitions.PetitionReply;
import app.caquita.landmark.trash_can.TrashCan;
import app.caquita.stats.GlobalStats;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        hikariConfig.setUsername(Config.DB_USER);
        hikariConfig.setPassword(Config.DB_PASSWORD);
        hikariConfig.setJdbcUrl(Config.DB_URL);

        JDBI = Jdbi.create(new HikariDataSource(hikariConfig));
        JDBI.installPlugin(new Jackson2Plugin());

        JDBI.registerRowMapper(ConstructorMapper.factory(User.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(User.Inventory.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Tool.class));

        JDBI.registerRowMapper(ConstructorMapper.factory(User.Currencies.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(UnlockedAvatar.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Friendship.class));

        // LANDMARKS
        JDBI.registerRowMapper(ConstructorMapper.factory(TrashCan.class));
        // LANDMARKS END

        // FORUMS
        //JDBI.registerRowMapper(ConstructorMapper.factory(Message.class));
        //JDBI.registerRowMapper(ConstructorMapper.factory(Reply.class));

        JDBI.registerRowMapper(ConstructorMapper.factory(PetitionMessage.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(PetitionReply.class));
        JDBI.registerRowMapper(ConstructorMapper.factory(Petition.class));
        // FORUMS END

        JDBI.registerRowMapper(ConstructorMapper.factory(GlobalStats.class));
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}