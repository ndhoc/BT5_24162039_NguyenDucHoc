package com.nguyenduchoc.bt5.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@Component
public class CloudinaryAvatarStorage {
    private static final Logger LOG = Logger.getLogger(CloudinaryAvatarStorage.class.getName());
    private static final String AVATAR_PREFIX = "shop-24162039/profiles/";
    private final Cloudinary client;

    public record Avatar(String url, String publicId) { }

    public CloudinaryAvatarStorage(@Value("${cloudinary.url:}") String url) {
        client = configuredClient(url);
    }

    public Avatar upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        ImageStorage.readUpload(file, "avatar");
        if (client == null) throw new FieldError("avatar", "Chưa cấu hình Cloudinary để tải ảnh đại diện.");
        String publicId = AVATAR_PREFIX + UUID.randomUUID();
        try (InputStream input = file.getInputStream()) {
            Map<?, ?> result = client.uploader().upload(input.readAllBytes(), ObjectUtils.asMap(
                    "resource_type", "image", "public_id", publicId, "overwrite", false));
            Object secureUrl = result.get("secure_url");
            if (secureUrl instanceof String url && publicId.equals(result.get("public_id"))
                    && url.length() <= 255 && isHttpsUrl(url)) return new Avatar(url, publicId);
        } catch (IOException | RuntimeException e) {
            discard(publicId);
            throw new FieldError("avatar", "Không tải được ảnh lên Cloudinary. Vui lòng thử lại.");
        }
        discard(publicId);
        throw new FieldError("avatar", "Cloudinary không trả về đường dẫn ảnh hợp lệ.");
    }

    public void discard(String publicId) {
        if (client == null || publicId == null || !publicId.startsWith(AVATAR_PREFIX)) return;
        try {
            client.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (IOException | RuntimeException e) {
            LOG.warning("Could not remove the obsolete Cloudinary profile image.");
        }
    }

    private static boolean isHttpsUrl(String value) {
        URI url = URI.create(value);
        return "https".equalsIgnoreCase(url.getScheme()) && url.getHost() != null && url.getUserInfo() == null;
    }

    private static Cloudinary configuredClient(String url) {
        try {
            if (url == null || url.isBlank()) return null;
            Cloudinary cloudinary = new Cloudinary(url.trim());
            cloudinary.config.timeout = 10;
            return cloudinary;
        } catch (RuntimeException e) {
            LOG.warning("Could not read Cloudinary configuration.");
            return null;
        }
    }
}
