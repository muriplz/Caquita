package com.kryeit.auth;

public enum TrustLevel {

    DEFAULT,
    TRUSTED,
    CONTRIBUTOR,
    MODERATOR,
    ADMINISTRATOR,
    ;

    public static User increaseTrust(User user) {
        int index = user.trust().ordinal();
        int newIndex = index + 1;

        TrustLevel trust = TrustLevel.values()[newIndex];
        return new User(user.id(), user.username(), user.password(), user.creation(), trust);
    }

    public static User decreaseTrust(User user) {
        int index = user.trust().ordinal();
        int newIndex = index - 1;

        TrustLevel trust = TrustLevel.values()[newIndex];
        return new User(user.id(), user.username(), user.password(), user.creation(), trust);
    }
}
