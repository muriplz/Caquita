package com.kryeit;

import com.kryeit.auth.Level;
import com.kryeit.auth.LoginApi;
import com.kryeit.auth.inventory.InventoryApi;
import com.kryeit.items.ItemConfigReader;
import com.kryeit.items.ItemsApi;
import com.kryeit.landmark.LandmarkApi;
import com.kryeit.landmark.can.TrashCanApi;
import com.kryeit.map.MapTileApi;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {
    public static void main(String[] args) {

        DatabaseUtils.createTables();
        new ItemConfigReader();

        SslPlugin sslPlugin = new SslPlugin(sslConfig -> {
            sslConfig.http2 = true;
            sslConfig.secure = false;
            sslConfig.insecurePort = Config.apiPort;
        });

        Javalin app = Javalin.create(config -> {
            config.registerPlugin(sslPlugin);

            config.validation.register(LocalDate.class, s -> {
                String[] split = s.split("-");
                int year = Integer.parseInt(split[0]);
                int month = Integer.parseInt(split[1]);
                int day = Integer.parseInt(split[2]);
                return LocalDate.of(year, month, day);
            });

            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowCredentials = true;
                    it.allowHost("https://caquita.app", "http://localhost:5173", "http://localhost:5174");
                });
            });

            config.validation.register(JSONObject.class, JSONObject::new);
            config.validation.register(UUID.class, UUID::fromString);

            config.showJavalinBanner = false;

            config.router.apiBuilder(() -> {
                path("api", () -> {
                    path("v1", () -> {
                        get(ctx -> ctx.result("Hello World!"));

                        path("items", () -> {
                            get(ItemsApi::getItems);
                            get("{id}", ItemsApi::getItem);
                        });

                        path("landmarks", () -> {
                            get(LandmarkApi::getLandmarks);

                            path("types", () -> {
                                get(LandmarkApi::getTypes);
                            });
                            path("{id}", () -> {
                                get(LandmarkApi::getLandmark);
                                patch(LandmarkApi::updateLandmark);
                            });

                            path("cans", () -> {
                                get(TrashCanApi::getCans);
                                put(TrashCanApi::createCan);
                            });
                        });

                        path("map", () -> {
                            path("tiles/{zoom}/{x}/{y}", () -> {
                                get(MapTileApi::getTile);
                            });
                        });

                        path("auth", () -> {
                            path("level", () -> {
                                get("{id}", Level::getLevel);
                                patch(Level::modifyLevel);
                            });
                            post("login", LoginApi::login);
                            post("register", LoginApi::register);
                            post("validate", LoginApi::validate);

                            path("inventory", () -> {
                                get(InventoryApi::getInventory);
                                post(InventoryApi::addItem);
                                delete(InventoryApi::removeItem);
                                patch(InventoryApi::moveItem);
                            });
                        });
                    });
                });
            });
        }).start();
    }
}