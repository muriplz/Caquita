package com.kryeit.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private static final String UPLOAD_DIR = "uploads";
    private static final String IMAGE_BASE_URL = "/api/v1/images";
    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;

    static {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    public static String processAndSaveImage(InputStream imageData) throws IOException {
        // Generate unique filename
        String filename = UUID.randomUUID().toString() + ".webp";
        Path destination = Paths.get(UPLOAD_DIR, filename);
        
        // Load and process image
        BufferedImage originalImage = ImageIO.read(imageData);
        if (originalImage == null) {
            throw new IOException("Invalid image format");
        }
        
        // Resize if needed
        BufferedImage resizedImage = resizeImage(originalImage);
        
        // Save as WebP
        try {
            // WebP writing uses ImageIO with TwelveMonkeys providing the WebP support
            ImageIO.write(resizedImage, "webp", destination.toFile());
            return filename;
        } catch (Exception e) {
            logger.error("Failed to save image as WebP, falling back to PNG", e);
            // Fallback to PNG if WebP fails
            String fallbackFilename = UUID.randomUUID().toString() + ".png";
            Path fallbackPath = Paths.get(UPLOAD_DIR, fallbackFilename);
            ImageIO.write(resizedImage, "png", fallbackPath.toFile());
            return fallbackFilename;
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Calculate new dimensions while maintaining aspect ratio
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            int targetWidth, targetHeight;

            // Determine scaling based on which dimension exceeds limits
            if (width > MAX_WIDTH && width > height) {
                targetWidth = MAX_WIDTH;
                targetHeight = (int) (height * ((double) MAX_WIDTH / width));
            } else if (height > MAX_HEIGHT) {
                targetHeight = MAX_HEIGHT;
                targetWidth = (int) (width * ((double) MAX_HEIGHT / height));
            } else if (width > MAX_WIDTH) {
                targetWidth = MAX_WIDTH;
                targetHeight = (int) (height * ((double) MAX_WIDTH / width));
            } else {
                targetWidth = width;
                targetHeight = height;
            }

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();

            // High-quality rendering hints
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            return resizedImage;
        }

        return originalImage;
    }
    
    public static String getImageUrl(String filename) {
        return IMAGE_BASE_URL + "/" + filename;
    }
    
    public static Path getImagePath(String filename) {
        return Paths.get(UPLOAD_DIR, filename);
    }
    
    public static boolean deleteImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        try {
            Path imagePath = getImagePath(filename);
            return Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            logger.error("Failed to delete image: {}", filename, e);
            return false;
        }
    }
}