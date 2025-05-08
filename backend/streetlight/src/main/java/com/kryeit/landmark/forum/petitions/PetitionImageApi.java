package com.kryeit.landmark.forum.petitions;

import com.kryeit.Database;
import com.kryeit.auth.AuthUtils;
import com.luciad.imageio.webp.WebPWriteParam;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class PetitionImageApi {
    private static final Logger logger = LoggerFactory.getLogger(PetitionImageApi.class);
    private static final String UPLOAD_DIR = "uploads/petitions";
    private static final String IMAGE_BASE_URL = "/api/v1/petitions/images";
    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;
    private static final float COMPRESSION_QUALITY = 0.9f;

    static {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                logger.info("Created directory: " + uploadDir.getAbsolutePath());
            } else {
                logger.error("Failed to create directory: " + uploadDir.getAbsolutePath());
            }
        }
    }

    public static void getImage(Context ctx) {
        String petitionId = ctx.pathParam("id");
        Path imagePath = Paths.get(UPLOAD_DIR, petitionId + ".webp");

        if (Files.exists(imagePath)) {
            try {
                byte[] imageData = Files.readAllBytes(imagePath);
                ctx.contentType("image/webp");
                ctx.result(imageData);
            } catch (IOException e) {
                ctx.status(500);
                ctx.result("Failed to read image file");
            }
        } else {
            ctx.status(404);
            ctx.result("Image not found");
        }
    }

    public static void uploadImage(Context ctx) {
        long userId = AuthUtils.getUser(ctx);
        long petitionId = ctx.pathParamAsClass("id", Long.class).get();

        boolean allowed = Database.getJdbi().withHandle(handle -> {
            return handle.createQuery("SELECT COUNT(*) FROM petitions WHERE id = :id AND user_id = :userId")
                    .bind("id", petitionId)
                    .bind("userId", userId)
                    .mapTo(Integer.class)
                    .one() > 0;
        });

        if (!allowed) {
            ctx.status(403);
            ctx.result("You don't have permission to upload images to this petition");
            return;
        }

        UploadedFile uploadedFile = ctx.uploadedFile("image");
        if (uploadedFile == null) {
            ctx.status(400);
            ctx.result("No image file provided");
            return;
        }

        String contentType = uploadedFile.contentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            ctx.status(415);
            ctx.result("Only image files are allowed");
            return;
        }

        try (InputStream is = uploadedFile.content()) {
            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) {
                ctx.status(400);
                ctx.result("Invalid image format");
                return;
            }

            BufferedImage processedImage = resizeImage(originalImage);

            Path destination = Paths.get(UPLOAD_DIR, petitionId + ".webp");
            compressToWebP(processedImage, destination);

            String imageUrl = IMAGE_BASE_URL + "/" + petitionId;
            Database.getJdbi().useHandle(handle -> {
                handle.createUpdate("UPDATE petitions SET image = :imageUrl WHERE id = :id")
                        .bind("imageUrl", imageUrl)
                        .bind("id", petitionId)
                        .execute();
            });

            ctx.status(200);
            ctx.json(java.util.Map.of("imageUrl", imageUrl));

        } catch (IOException e) {
            ctx.status(500);
            ctx.result("Failed to process image: " + e.getMessage());
        }
    }

    public static void deleteImage(long petitionId) {
        Path imagePath = Paths.get(UPLOAD_DIR, petitionId + ".webp");

        if (Files.exists(imagePath)) {
            try {
                Files.delete(imagePath);
                logger.info("Deleted image file for petition ID: " + petitionId);
            } catch (IOException e) {
                logger.error("Failed to delete image for petition ID: " + petitionId, e);
            }
        } else {
            logger.info("No image file found for petition ID: " + petitionId);
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            double scaleFactor = Math.min(
                    (double) MAX_WIDTH / width,
                    (double) MAX_HEIGHT / height
            );

            int newWidth = (int) (width * scaleFactor);
            int newHeight = (int) (height * scaleFactor);

            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            return resizedImage;
        }

        return originalImage;
    }

    private static void compressToWebP(BufferedImage image, Path destination) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");

        if (!writers.hasNext()) {
            throw new IOException("No WebP writer available");
        }

        ImageWriter writer = writers.next();

        WebPWriteParam writeParam =
                (WebPWriteParam) writer.getDefaultWriteParam();

        writeParam.setCompressionType("Lossy");

        writeParam.setCompressionQuality(COMPRESSION_QUALITY);

        try (FileImageOutputStream output = new FileImageOutputStream(destination.toFile())) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }
    }

    public static void acceptImage(long petitionId, long landmarkId) {
        Path petitionImagePath = Paths.get(UPLOAD_DIR, petitionId + ".webp");

        File landmarksDir = new File("uploads/landmarks");
        if (!landmarksDir.exists()) {
            boolean created = landmarksDir.mkdirs();
            if (!created) {
                return;
            }
        }

        Path landmarkImagePath = Paths.get("uploads/landmarks", landmarkId + ".webp");

        if (Files.exists(petitionImagePath)) {
            try {
                Files.copy(petitionImagePath, landmarkImagePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
            }
        } else {
        }
    }
}