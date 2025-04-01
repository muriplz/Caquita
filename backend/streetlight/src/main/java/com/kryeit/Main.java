package com.kryeit;

import com.kryeit.auth.AuthUtils;
import com.kryeit.auth.LoginApi;
import com.kryeit.auth.currency.CurrencyService;
import com.kryeit.auth.currency.CurrencySyncProvider;
import com.kryeit.auth.inventory.InventoryApi;
import com.kryeit.content.items.ItemsApi;
import com.kryeit.landmark.LandmarkApi;
import com.kryeit.landmark.forum.petitions.PetitionsApi;
import com.kryeit.registry.CaquitaItems;
import com.kryeit.sync.SyncManager;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    public static SyncManager syncManager = new SyncManager();

    public static void main(String[] args) {

        DatabaseUtils.createTables();
        CaquitaItems.register();

        CurrencyService currencyService = new CurrencyService(Database.getJdbi());
        syncManager.registerDataProvider("currencies", new CurrencySyncProvider(currencyService));

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

            config.router.apiBuilder(Main::apiRoutes);
        }).start();

        syncManager.setupWebSockets(app);
    }

    public static void apiRoutes() {
        path("api/v1", () -> {
            get(ctx -> ctx.result("Hello from Streetlight!"));

            path("items", () -> {
                get(ctx -> ctx.json(CaquitaItems.getAllItems()));
                get("{id}", ItemsApi::getItem);
            });

            landmarkRoutes();

            authRoutes();

            petitionPortalRoutes();
        });
    }

    private static void petitionPortalRoutes() {

        path("petitions", () -> {
            get(PetitionsApi::getPetitions);

            post(PetitionsApi::createPetition);
            path("{id}", () -> {
                get(PetitionsApi::getPetition);
                get("messages", PetitionsApi.MessagesApi::getMessages);
                patch("status", PetitionsApi::updateStatus);
                patch(PetitionsApi::updatePetition);
                delete(PetitionsApi::deletePetition);

                post("accept", PetitionsApi::acceptPetition);
            });
        });

    }

    private static void authRoutes() {
        path("auth", () -> {

            get(AuthUtils::getUsername);

            post("login", LoginApi::login);
            post("register", LoginApi::register);
            post("validate", LoginApi::validate);

            path("inventory", () -> {
                get(InventoryApi::getInventory);
                post(InventoryApi::addItem);
                delete(InventoryApi::removeItem);
                patch(InventoryApi::moveItem);
                post("rotate", InventoryApi::rotateItem);
                post("can-place", InventoryApi::canPlaceItem);
            });
        });
    }

    private static void landmarkRoutes() {
        path("landmarks", () -> {
            get(LandmarkApi::getLandmarks);

            path("{id}", () -> {
                get(LandmarkApi::getLandmark);
                patch(LandmarkApi::updateLandmark);
            });
        });
    }
}