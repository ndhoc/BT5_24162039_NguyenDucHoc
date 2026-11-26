package com.nguyenduchoc.bt5.service.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.nguyenduchoc.bt5.entity.Video;
import com.nguyenduchoc.bt5.repository.IVideoRepository;
import com.nguyenduchoc.bt5.service.VideoService;
import com.nguyenduchoc.bt5.validation.FormValidation;
import com.nguyenduchoc.bt5.validation.FormValidation.FieldError;

@org.springframework.stereotype.Service
public class VideoServiceImpl implements VideoService {
    private final IVideoRepository repository;

    public VideoServiceImpl(IVideoRepository repository) { this.repository = repository; }

    private void validate(Video video, boolean requireFile) {
        video.setTitle(FormValidation.requiredText("title", video.getTitle(), 255, "Tiêu đề video"));
        video.setDescription(FormValidation.description(video.getDescription()));
        if (requireFile && (video.getFileName() == null || video.getFileName().isBlank()))
            throw new FieldError("videoFile", "Vui lòng chọn tệp video.");
        if (video.getUser() == null) throw new FieldError("userId", "Video phải thuộc về một tài khoản.");
        if (video.getCreatedAt() == null) video.setCreatedAt(Date.from(Instant.now()));
    }

    @Override public void insert(Video video) { validate(video, true); repository.insert(video); }
    @Override public void update(Video video) { validate(video, false); repository.update(video); }
    @Override public void delete(int id) { repository.delete(id); }
    @Override public Video get(int id) { return repository.get(id); }
    @Override public List<Video> latest(int limit) {
        if (limit <= 0) return List.of();
        return repository.latest(limit);
    }
    @Override public List<Video> all() { return repository.all(); }
    @Override public long countByUser(int userId) { return repository.countByUser(userId); }
    @Override public long count() { return repository.count(); }

    @Override
    public List<Video> page(int page) {
        int safePage = Math.max(1, page);
        return repository.page((safePage - 1) * PAGE_SIZE, PAGE_SIZE);
    }

    @Override
    public int pageCount() {
        long total = repository.count();
        return (int) Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    @Override
    public List<Video> pageByUser(int userId, int page) {
        int safePage = Math.max(1, page);
        return repository.pageByUser(userId, (safePage - 1) * PAGE_SIZE, PAGE_SIZE);
    }

    @Override
    public int pageCountByUser(int userId) {
        long total = repository.countByUser(userId);
        return (int) Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
    }
}
