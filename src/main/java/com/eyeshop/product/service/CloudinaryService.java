package com.eyeshop.product.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    /**
     * Upload image to Cloudinary.
     * Returns [secureUrl, publicId] tuple.
     */
    UploadResult upload(MultipartFile file);

    /** Delete image from Cloudinary by public ID. Returns true if deleted. */
    boolean delete(String publicId);

    record UploadResult(String url, String publicId) {}
}