// CarvingObstacle.java
package app.caquita.carving.obstacles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface CarvingObstacle {

    @JsonProperty("id")
    String id();

    @JsonProperty("shape")
    int[][] shape();

    @JsonIgnore
    default void trigger() {
        System.out.println("DEBUG: Obstacle '" + id() + "' triggered!");
    }
}