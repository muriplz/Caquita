// CarvingObstacle.java
package app.caquita.carving.obstacles;

import com.fasterxml.jackson.annotation.JsonProperty;
import app.caquita.carving.CarvingSite;

public interface CarvingObstacle {

    @JsonProperty("id")
    String id();

    @JsonProperty("shape")
    int[][] shape();

    @JsonProperty("type")
    CarvingSite.SiteType type();
}