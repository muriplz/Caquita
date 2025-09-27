package app.caquita.auth.inventory.clothes;

import app.caquita.auth.inventory.clothes.dusters.EquippedDuster;
import app.caquita.storage.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;

import java.util.List;

public record Wardrobe(
        long id, long userId,
        JSONObject duster,
        List<String> dusters
) {
    public static void init(long userId) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("""
                        INSERT INTO wardrobes (user_id, duster, dusters)
                        VALUES (:userId, '{}', '[]'::jsonb)
                        """)
                        .bind("userId", userId)
                        .execute()
        );
    }

    public static Wardrobe get(long userId) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM wardrobes WHERE user_id = :user_id")
                        .bind("user_id", userId)
                        .mapTo(Wardrobe.class)
                        .findFirst()
                        .orElseGet(() -> {
                            init(userId);
                            return get(userId);
                        })
        );
    }



    public EquippedDuster getDuster() throws JsonProcessingException {
        return Database.fromJsonObject(duster, EquippedDuster.class);
    }

}
