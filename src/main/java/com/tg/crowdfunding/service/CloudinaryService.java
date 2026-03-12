package com.tg.crowdfunding.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${app.cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    public String uploadImage(byte[] bytes, String folder) throws IOException {
        Map<?, ?> result = cloudinary.uploader().upload(bytes,
            ObjectUtils.asMap("folder", folder, "resource_type", "image"));
        return (String) result.get("secure_url");
    }
}
