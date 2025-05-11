package com.kryeit.content.items;

public interface MaterialClassification {
    String getId();

    String getName();

    default Integer getCode() {
        return null;
    }

    default boolean isRecyclable() {
        return false;
    }
}