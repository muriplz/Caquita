package app.caquita.registry;

import app.caquita.carving.obstacles.CarvingObstacle;
import app.caquita.carving.obstacles.ManholeCover;
import app.caquita.carving.obstacles.MetalPole;
import app.caquita.carving.obstacles.Sprinkler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AllObstacles {
    private static final Map<String, CarvingObstacle> OBSTACLES = new HashMap<>();

    public static void register(CarvingObstacle obstacle) {
        OBSTACLES.put(obstacle.id(), obstacle);
    }

    public static CarvingObstacle getObstacle(String id) {
        return OBSTACLES.get(id);
    }

    public static Collection<CarvingObstacle> getAllObstacles() {
        return OBSTACLES.values();
    }

    public static boolean isRegistered(String id) {
        return OBSTACLES.containsKey(id);
    }

    static {
        register(new ManholeCover());
        register(new Sprinkler());
        register(new MetalPole());
    }

    public static void register() {
    }
}
