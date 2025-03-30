package com.kryeit.content.items;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface MaterialClassification {
    String getId();

    String getName();

    default Integer getCode() {
        return null; // Default implementation returns null
    }
}