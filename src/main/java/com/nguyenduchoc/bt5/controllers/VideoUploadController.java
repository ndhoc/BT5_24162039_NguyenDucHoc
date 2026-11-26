package com.nguyenduchoc.bt5.controllers;

import java.io.IOException;
import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.entity.Video;
import com.nguyenduchoc.bt5.service.VideoService;
import com.nguyenduchoc.bt5.utils.VideoStorage;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@Controller
public class VideoUploadController {
    private static final Logger LOG = LoggerFactory.getLogger(VideoUploadController.class);
    private final VideoService videos;

    public VideoUploadController(VideoService videos) { this.videos = videos; }

    @GetMapping("/video/upload") String form(HttpSession session) {
        return session.getAttribute("account") == null ? "redirect:/login" : "legacy/video-upload";
    }

    @PostMapping("/video/upload") String save(HttpServletRequest req,
            @RequestParam(required = false) MultipartFile videoFile) throws IOException {
        User account = (User) req.getSession().getAttribute("account");
        if (account == null) return "redirect:/login";
        String stored = null;
        boolean persisted = false;
        try {
            Video video = new Video();
            video.setTitle(FormValidation.requiredText("title", req.getParameter("title"), 255, "Tiêu đề video"));
            video.setDescription(FormValidation.description(req.getParameter("description")));
            video.setUser(account);
            video.setCreatedAt(Instant.now());
            stored = VideoStorage.save(videoFile);
            if (stored == null) throw new FieldError("videoFile", "Vui lòng chọn tệp video.");
            video.setFileName(stored);
            videos.insert(video);
            persisted = true;
            return "redirect:/video?saved=1";
        } catch (IllegalArgumentException e) {
            cleanup(stored);
            FormErrors.show(req, e);
            req.setAttribute("titleValue", req.getParameter("title"));
            req.setAttribute("descriptionValue", req.getParameter("description"));
            return "legacy/video-upload";
        } catch (RuntimeException | IOException e) {
            if (!persisted) cleanup(stored);
            throw e;
        }
    }
    private void cleanup(String file) {
        try { VideoStorage.delete(file); }
        catch (IOException e) { LOG.warn("Could not remove failed video upload {}", file, e); }
    }
}
