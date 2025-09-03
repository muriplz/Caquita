package app.caquita.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import app.caquita.storage.Database;

public record Level(
        @JsonProperty("level") int level,
        @JsonProperty("experience") int experience,
        @JsonProperty("progress") int progress,
        @JsonProperty("total") int total
) {
    public static final int[] LEVEL_REQUIRED_EXP = {
            0,     // Level 0->1
            1000,  // Level 1->2
            3000,  // Level 2->3
            6000,  // Level 3->4
            10000, // Level 4->5
            15000, // Level 5->6
            21000, // Level 6->7
            28000, // Level 7->8
            36000, // Level 8->9
            45000, // Level 9->10
            55000, // Level 10->11
            65000, // Level 11->12
            75000, // Level 12->13
            85000, // Level 13->14
            100000, // Level 14->15
            120000, // Level 15->16
            140000, // Level 16->17
            160000, // Level 17->18
            185000, // Level 18->19
            210000, // Level 19->20
            260000, // Level 20->21
            335000, // Level 21->22
            435000, // Level 22->23
            560000, // Level 23->24
            710000, // Level 24->25
            900000, // Level 25->26
            1100000, // Level 26->27
            1350000, // Level 27->28
            1650000, // Level 28->29
            2000000, // Level 29->30
            2500000, // Level 30->31
            3000000, // Level 31->32
            3750000, // Level 32->33
            4750000, // Level 33->34
            6000000, // Level 34->35
            7500000, // Level 35->36
            9500000, // Level 36->37
            12000000, // Level 37->38
            15000000, // Level 38->39
            20000000  // Level 39->40
    };

    private static final int MAX_LEVEL = 40;

    public Level() {
        this(1, 0, 0, LEVEL_REQUIRED_EXP[0]);
    }

    public static Level fromExperience(int totalExperience) {
        int level = calculateLevel(totalExperience);
        int expForPreviousLevels = getTotalExpForLevel(level - 1);
        int levelProgress = totalExperience - expForPreviousLevels;
        int levelTotal = level < MAX_LEVEL ? LEVEL_REQUIRED_EXP[level - 1] : LEVEL_REQUIRED_EXP[MAX_LEVEL - 1];

        return new Level(level, totalExperience, levelProgress, levelTotal);
    }

    private static int calculateLevel(int exp) {
        int level = 1;
        int totalExp = 0;

        while (level < MAX_LEVEL && totalExp + LEVEL_REQUIRED_EXP[level - 1] <= exp) {
            totalExp += LEVEL_REQUIRED_EXP[level - 1];
            level++;
        }

        return level;
    }

    private static int getTotalExpForLevel(int targetLevel) {
        int totalExp = 0;
        for (int i = 0; i < targetLevel; i++) {
            totalExp += i < LEVEL_REQUIRED_EXP.length ? LEVEL_REQUIRED_EXP[i] : LEVEL_REQUIRED_EXP[LEVEL_REQUIRED_EXP.length - 1];
        }
        return totalExp;
    }

    public User.Trust getTrustLevel() {
        if (level >= 30) return User.Trust.MODERATOR;
        if (level >= 25) return User.Trust.CONTRIBUTOR;
        if (level >= 20) return User.Trust.TRUSTED;
        return User.Trust.DEFAULT;
    }

    public ObjectNode toJson() {
        ObjectNode json = Database.MAPPER.createObjectNode();
        json.put("level", level);
        json.put("experience", experience);
        json.put("progress", progress);
        json.put("total", total);
        return json;
    }
}