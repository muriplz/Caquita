package com.kryeit.sync;

import com.kryeit.auth.AuthUtils;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import com.google.gson.Gson;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SyncManager {
    private static final Gson gson = new Gson();
    private final Map<Long, Set<String>> userSubscriptions = new ConcurrentHashMap<>();
    private final Map<Long, WsContext> sessions = new ConcurrentHashMap<>();
    private final Map<String, SyncDataProvider> dataProviders = new ConcurrentHashMap<>();

    public void setupWebSockets(Javalin app) {
        app.ws("/api/sync", ws -> {
            ws.onConnect(ctx -> {
                Long userId = AuthUtils.getUser(ctx.getUpgradeCtx$javalin());
                sessions.put(userId, ctx);
                userSubscriptions.putIfAbsent(userId, ConcurrentHashMap.newKeySet());
            });

            ws.onMessage(ctx -> {
                Long userId = AuthUtils.getUser(ctx.getUpgradeCtx$javalin());
                SyncMessage message = gson.fromJson(ctx.message(), SyncMessage.class);

                if ("SUBSCRIBE".equals(message.type)) {
                    userSubscriptions.get(userId).add(message.entity);
                    sendInitialData(userId, message.entity);
                } else if ("UNSUBSCRIBE".equals(message.type)) {
                    userSubscriptions.get(userId).remove(message.entity);
                }
            });

            ws.onClose(ctx -> {
                Long userId = AuthUtils.getUser(ctx.getUpgradeCtx$javalin());
                sessions.remove(userId);
                userSubscriptions.remove(userId);
            });
        });
    }

    private void sendInitialData(Long userId, String entity) {
        SyncDataProvider provider = dataProviders.get(entity);
        if (provider != null) {
            Object data = provider.getInitialData(userId);
            if (data != null) {
                publishUpdate(userId, entity, data);
            }
        }
    }

    public void registerDataProvider(String entity, SyncDataProvider provider) {
        dataProviders.put(entity, provider);
    }

    public <T> void publishUpdate(Long userId, String entity, T data) {
        WsContext ctx = sessions.get(userId);
        if (ctx != null && ctx.session.isOpen() &&
                (userSubscriptions.get(userId) == null || userSubscriptions.get(userId).contains(entity))) {

            SyncMessage message = new SyncMessage("UPDATE", entity, data);
            ctx.send(gson.toJson(message));
        }
    }

    public static class SyncMessage {
        public String type;
        public String entity;
        public Object data;

        public SyncMessage(String type, String entity, Object data) {
            this.type = type;
            this.entity = entity;
            this.data = data;
        }
    }

    public interface SyncDataProvider {
        Object getInitialData(Long userId);
    }
}