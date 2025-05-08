package com.kryeit.content.items;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface MaterialClassification {
    String getId();

    String getName();

    default Integer getCode() {
        return null; // Default implementation returns null
    }
}