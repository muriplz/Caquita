package app.caquita;

import app.caquita.auth.AuthUtils;
import app.caquita.auth.FriendshipApi;
import app.caquita.auth.LoginApi;
import app.caquita.auth.avatar.UnlockedAvatar;
import app.caquita.auth.inventory.InventoryApi;
import app.caquita.landmark.forum.petitions.PetitionImageApi;
import app.caquita.landmark.forum.petitions.PetitionsApi;
import app.caquita.landmark.trash_can.TrashCanApi;
import app.caquita.map.MapApi;
import app.caquita.registry.AllItems;
import app.caquita.stats.GlobalStats;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.UUID;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    public static void main(String[] args) {

        DatabaseUtils.createTables();
        AllItems.register();


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

    }

    public static void apiRoutes() {
        path("api/v1", () -> {
            get(ctx -> ctx.result("Hello from Streetlight!"));

            path("item-kinds", () -> {
                get(ctx -> ctx.status(200).json(AllItems.getAllItems()));
                get("{id}", ctx -> ctx.status(200).json(AllItems.getItem(ctx.pathParam("id"))));
            });

            path("inventory", () -> {
                get(InventoryApi::getInventory);
                post(InventoryApi::rotateItem);
                put(InventoryApi::addItem);
                delete(InventoryApi::removeItem);
                patch(InventoryApi::moveItem);
                post("can-place", InventoryApi::canPlaceItem);
            });

            landmarkRoutes();

            authRoutes();

            mapRoutes();

            petitionPortalRoutes();

            path("stats", () -> {
                get(GlobalStats::getAll);
//                get("onlines", ctx -> ctx.json(syncManager.sessions.size()));
                get("global", GlobalStats::get);
                post("record", GlobalStats::recordView);
            });
        });
    }

    private static void mapRoutes() {
        path("map", () -> {
            get("16/{x}/{y}", MapApi::getTile);
        });
    }

    private static void petitionPortalRoutes() {

        path("petitions", () -> {
            get(PetitionsApi::getPetitions);

            post(PetitionsApi::createPetition);
            path("{id}", () -> {
                path("image", () -> {
                    get(PetitionImageApi::getImage);
                    post(PetitionImageApi::uploadImage);
                });
                get(PetitionsApi::getPetition);
                post("send-message", PetitionsApi::sendMessage);
                post("send-reply", PetitionsApi::sendReply);
                get("messages", PetitionsApi.MessagesApi::getMessages);
                get("replies", PetitionsApi.MessagesApi::getReplies);
                patch("status", PetitionsApi::updateStatus);
                patch(PetitionsApi::updatePetition);
                delete(PetitionsApi::deletePetition);
            });
        });

    }

    private static void authRoutes() {
        path("auth", () -> {

            get(AuthUtils::getUsername);

            post("login", LoginApi::login);
            post("register", LoginApi::register);
            post("validate", LoginApi::validate);

            get("avatars", UnlockedAvatar::getUnlockedAvatars);
            post("avatars", UnlockedAvatar::changeAvatar);

            path("friends", () -> {
                get(FriendshipApi::getFriends);
                get("avatars", FriendshipApi::getAvatars);
                get("pending", FriendshipApi::getPendingRequests);
                get("sent", FriendshipApi::getSentRequests);
                post("request", FriendshipApi::sendRequest);
                post("respond", FriendshipApi::respondToRequest);
                delete("{friendId}", FriendshipApi::removeFriend);
                post("block", FriendshipApi::blockUser);
            });
        });
    }

    private static void landmarkRoutes() {
        path("landmarks", () -> {
            path("trash_cans", () -> {
                get(TrashCanApi::get);

                path("{id}", () -> {

                });
            });
        });
    }
}