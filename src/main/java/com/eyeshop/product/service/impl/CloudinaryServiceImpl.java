package com.eyeshop.product.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.eyeshop.product.exception.ImageUploadException;
import com.eyeshop.product.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${app.cloudinary.folder:eyeshop/products}")
    private String folder;

    @Value("${app.cloudinary.max-file-size-mb:5}")
    private int maxFileSizeMb;

    @Value("${app.cloudinary.allowed-formats:jpg,jpeg,png,webp}")
    private List<String> allowedFormats;

    @Override
    public UploadResult upload(MultipartFile file) {
        validateFile(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", true
                )
            );

            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");

            log.info("Image uploaded to Cloudinary: publicId={}", publicId);
            return new UploadResult(url, publicId);

        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new ImageUploadException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return false;
        }
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            boolean deleted = "ok".equals(result.get("result"));
            if (deleted) {
                log.info("Image deleted from Cloudinary: publicId={}", publicId);
            } else {
                log.warn("Image deletion returned non-ok: publicId={}, result={}", publicId, result.get("result"));
            }
            return deleted;
        } catch (IOException e) {
            log.error("Cloudinary delete failed: publicId={}", publicId, e);
            return false;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImageUploadException("File is empty");
        }

        // Size check
        long maxBytes = (long) maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new ImageUploadException(
                String.format("File too large. Maximum allowed: %d MB", maxFileSizeMb));
        }

        // Format check
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageUploadException("File must be an image");
        }

        String ext = contentType.substring("image/".length()).toLowerCase();
        if (!allowedFormats.contains(ext) && !"jpeg".equals(ext)) {
            throw new ImageUploadException(
                "Unsupported image format. Allowed: " + allowedFormats);
        }
    }
}