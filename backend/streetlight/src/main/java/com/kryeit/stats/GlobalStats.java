package com.kryeit.stats;

import com.kryeit.Config;
import com.kryeit.Database;
import io.javalin.http.Context;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public record GlobalStats(GlobalStat stat, long value) {

    public enum GlobalStat {
        VIEWS,
        USERS,
        TRASH_CANS,
    }

    static {
        int userCount = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM users")
                        .mapTo(Integer.class)
                        .one()
        );

        int trashCanCount = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM trash_cans")
                        .mapTo(Integer.class)
                        .one()
        );

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE global_stats SET value = :value WHERE stat = 'USERS'")
                        .bind("value", userCount)
                        .execute()
        );

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE global_stats SET value = :value WHERE stat = 'TRASH_CANS'")
                        .bind("value", trashCanCount)
                        .execute()
        );
    }

    private static final ConcurrentHashMap<String, Long> viewThrottleCache = new ConcurrentHashMap<>();
    private static final long VIEW_THROTTLE_MS = TimeUnit.MINUTES.toMillis(5);

    public static void get(Context ctx) {
        String statString = new JSONObject(ctx.body()).getString("stat");
        GlobalStat stat = GlobalStat.valueOf(statString.toUpperCase());

        long value = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT value FROM global_stats WHERE stat = :stat")
                        .bind("stat", stat.name())
                        .mapTo(Long.class)
                        .findOne()
                        .orElse(0L)
        );

        ctx.json(Map.of("value", value));
    }

    public static void getAll(Context ctx) {
        Map<String, Long> stats = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT stat, value FROM global_stats")
                        .reduceRows(new HashMap<>(),
                                (map, row) -> {
                                    map.put(row.getColumn("stat", String.class),
                                            row.getColumn("value", Long.class));
                                    return map;
                                })
        );

        ctx.json(stats);
    }

    public static void recordView(Context ctx) {
        String clientId = hashIp(ctx.ip());

        Long lastRecorded = viewThrottleCache.get(clientId);
        long now = System.currentTimeMillis();

        if (lastRecorded != null && now - lastRecorded < VIEW_THROTTLE_MS) {
            ctx.json(Map.of("success", true));
            return;
        }

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE global_stats SET value = value + 1 WHERE stat = 'VIEWS'")
                        .execute()
        );

        viewThrottleCache.put(clientId, now);

        if (Math.random() < 0.01) {
            long cleanupTime = now - VIEW_THROTTLE_MS * 2;
            viewThrottleCache.entrySet().removeIf(entry -> entry.getValue() < cleanupTime);
        }

        ctx.json(Map.of("success", true));
    }

    private static String hashIp(String ip) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((ip + Config.ipSalt).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return ip.replaceAll("\\.", "");
        }
    }

    public static void incrementStat(GlobalStat stat) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE global_stats SET value = value + 1 WHERE stat = :stat")
                        .bind("stat", stat.name())
                        .execute()
        );
    }
}