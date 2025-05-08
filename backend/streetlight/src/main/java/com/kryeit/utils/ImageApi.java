package com.kryeit.utils;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageApi {

    public static void getImage(Context ctx) {
        String filename = ctx.pathParam("filename");
        Path imagePath = ImageService.getImagePath(filename);

        if (Files.exists(imagePath)) {
            try {
                byte[] imageData = Files.readAllBytes(imagePath);
                String mimeType = getMimeType(filename);
                ctx.contentType(mimeType);
                ctx.result(imageData);
            } catch (IOException e) {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
                ctx.result("Failed to read image file");
            }
        } else {
            ctx.status(HttpStatus.NOT_FOUND);
            ctx.result("Image not found");
        }
    }

    public static void uploadPetitionImage(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        long petitionId = ctx.pathParamAsClass("id", Long.class).get();

        // Check if petition exists and user owns it
        boolean allowed = Database.getJdbi().withHandle(handle -> {
            return handle.createQuery("SELECT COUNT(*) FROM petitions WHERE id = :id AND user_id = :userId")
                    .bind("id", petitionId)
                    .bind("userId", userId)
                    .mapTo(Integer.class)
                    .one() > 0;
        });

        if (!allowed) {
            ctx.status(HttpStatus.FORBIDDEN);
            ctx.result("You don't have permission to upload images to this petition");
            return;
        }

        UploadedFile uploadedFile = ctx.uploadedFile("image");
        if (uploadedFile == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("No image file provided");
            return;
        }

        // Use a try-with-resources to ensure the input stream is closed
        try (InputStream is = uploadedFile.content()) {
            // Delete previous image if exists
            String currentImage = Database.getJdbi().withHandle(handle ->
                    handle.createQuery("SELECT image FROM petitions WHERE id = :id")
                            .bind("id", petitionId)
                            .mapTo(String.class)
                            .findOne()
                            .orElse(null)
            );

            if (currentImage != null && !currentImage.isEmpty()) {
                ImageService.deleteImage(currentImage);
            }

            // Process and save new image
            String filename = ImageService.processAndSaveImage(is);
            String imageUrl = ImageService.getImageUrl(filename);

            // Update petition with new image
            Database.getJdbi().useHandle(handle -> {
                handle.createUpdate("UPDATE petitions SET image = :image WHERE id = :id")
                        .bind("image", filename)
                        .bind("id", petitionId)
                        .execute();
            });

            ctx.status(HttpStatus.OK);
            ctx.json(java.util.Map.of("imageUrl", imageUrl));

        } catch (IOException e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.result("Failed to process image: " + e.getMessage());
        }

        // Force cleanup of temporary files
        try {
            System.gc();
        } catch (Exception e) {
            // Ignore
        }
    }

    private static String getMimeType(String filename) {
        if (filename.endsWith(".webp")) {
            return "image/webp";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
        }
    }
}