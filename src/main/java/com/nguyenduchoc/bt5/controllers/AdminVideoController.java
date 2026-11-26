package com.nguyenduchoc.bt5.controllers;

import java.io.IOException;
import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.nguyenduchoc.bt5.entity.User;
import com.nguyenduchoc.bt5.entity.Video;
import com.nguyenduchoc.bt5.service.UserService;
import com.nguyenduchoc.bt5.service.VideoService;
import com.nguyenduchoc.bt5.utils.VideoStorage;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@Controller
public class AdminVideoController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminVideoController.class);
    private final VideoService videos;
    private final UserService users;

    public AdminVideoController(VideoService videos, UserService users) { this.videos = videos; this.users = users; }

    @GetMapping("/admin/video/list") String list(HttpServletRequest req) {
        String keyword = req.getParameter("q");
        var items = videos.all();
        if (keyword != null && !keyword.isBlank()) {
            String needle = keyword.trim().toLowerCase();
            items = items.stream().filter(v -> v.getTitle().toLowerCase().contains(needle)
                    || v.getUser() != null && v.getUser().getUsername().toLowerCase().contains(needle)).toList();
        }
        req.setAttribute("videoList", items);
        return "legacy/admin/list-video";
    }
    @GetMapping("/admin/video/add") String add(HttpServletRequest req) { return form(req, null, false); }
    @GetMapping("/admin/video/edit") String edit(HttpServletRequest req) { return form(req, existing(req), true); }
    @GetMapping("/admin/video/delete") String confirmDelete(HttpServletRequest req) {
        req.setAttribute("video", existing(req)); req.setAttribute("userList", users.all());
        return "legacy/admin/delete-video";
    }
    @PostMapping("/admin/video/add") String addPost(HttpServletRequest req,
            @RequestParam(required = false) MultipartFile videoFile) throws IOException { return save(req, videoFile, false); }
    @PostMapping("/admin/video/edit") String editPost(HttpServletRequest req,
            @RequestParam(required = false) MultipartFile videoFile) throws IOException { return save(req, videoFile, true); }
    @PostMapping("/admin/video/delete") String delete(HttpServletRequest req) {
        Video target = existing(req);
        videos.delete(target.getId());
        cleanup(target.getFileName());
        return "redirect:/admin/video/list?saved=delete";
    }

    private String save(HttpServletRequest req, MultipartFile file, boolean editing) throws IOException {
        Video video = editing ? existing(req) : new Video();
        String oldFile = editing ? video.getFileName() : null;
        String newFile = null;
        boolean persisted = false;
        try {
            video.setTitle(FormValidation.requiredText("title", req.getParameter("title"), 255, "Tiêu đề video"));
            video.setDescription(FormValidation.description(req.getParameter("description")));
            int userId = FormValidation.positiveId("userId", req.getParameter("userId"), "Vui lòng chọn chủ sở hữu hợp lệ.");
            User owner = users.getById(userId);
            if (owner == null) throw new FieldError("userId", "Tài khoản chủ sở hữu không tồn tại.");
            video.setUser(owner);
            if (video.getCreatedAt() == null) video.setCreatedAt(Instant.now());
            newFile = VideoStorage.save(file);
            if (newFile != null) video.setFileName(newFile);
            else if (oldFile == null) throw new FieldError("videoFile", "Vui lòng chọn tệp video.");
            if (editing) videos.update(video); else videos.insert(video);
            persisted = true;
            if (newFile != null && oldFile != null) cleanup(oldFile);
            return "redirect:/admin/video/list?saved=" + (editing ? "edit" : "add");
        } catch (IllegalArgumentException e) {
            cleanup(newFile);
            FormErrors.show(req, e);
            return form(req, video, editing);
        } catch (RuntimeException | IOException e) {
            if (!persisted) cleanup(newFile);
            throw e;
        }
    }
    private String form(HttpServletRequest req, Video video, boolean editing) {
        req.setAttribute("video", video);
        req.setAttribute("editing", editing);
        req.setAttribute("userList", users.all());
        return "legacy/admin/video-form";
    }
    private Video existing(HttpServletRequest req) {
        int id;
        try { id = FormValidation.positiveId("id", req.getParameter("id"), "Mã video không hợp lệ."); }
        catch (IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()); }
        Video video = videos.get(id);
        if (video == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video không tồn tại.");
        return video;
    }
    private void cleanup(String file) {
        try { VideoStorage.delete(file); }
        catch (IOException e) { LOG.warn("Could not remove video file {}", file, e); }
    }
}
