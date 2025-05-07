package com.kryeit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigReader.class);

    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;

    public static void readFile(Path path) throws IOException {
        try {
            String config = readOrCopyFile(path.resolve("config.json"), "/config.json");
            JSONObject configObject = new JSONObject(config);

            DB_URL = configObject.getString("db-url");
            DB_USER = configObject.getString("db-user");
            DB_PASSWORD = configObject.getString("db-password");

        } catch (Exception e) {
            LOGGER.error("Error reading config file", e);
        }
    }

    public static String readOrCopyFile(Path path, String exampleFile) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            LOGGER.info("File does not exist, attempting to copy from resources: " + exampleFile);
            InputStream stream = ConfigReader.class.getResourceAsStream(exampleFile);
            if (stream == null) {
                LOGGER.error("Cannot load example file: " + exampleFile);
                throw new NullPointerException("Cannot load example file");
            }

            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            Files.copy(stream, path);
            LOGGER.info("File copied to: " + path.toString());
        } else {
            LOGGER.info("File already exists: " + path.toString());
        }
        return Files.readString(path);
    }
}