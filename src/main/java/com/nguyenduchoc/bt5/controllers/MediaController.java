package com.nguyenduchoc.bt5.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;
import com.nguyenduchoc.bt5.utils.ImageStorage;
import com.nguyenduchoc.bt5.utils.VideoStorage;

@Controller
public class MediaController {
    private final WebApplicationContext context;
    public MediaController(WebApplicationContext context) { this.context = context; }

    @GetMapping("/image") ResponseEntity<Resource> categoryImage(@RequestParam String fname) throws IOException {
        return image(fname, ImageStorage.directory());
    }
    @GetMapping("/product-image/{name}") ResponseEntity<Resource> productImage(@PathVariable String name) throws IOException {
        return image(name, ImageStorage.productDirectory());
    }
    private ResponseEntity<Resource> image(String name, Path directory) throws IOException {
        if (!ImageStorage.isStoredName(name)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Path file = directory.resolve(name);
        if (!Files.isRegularFile(file)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).contentLength(Files.size(file))
                .body(new FileSystemResource(file));
    }

    @GetMapping("/video-file") ResponseEntity<Resource> video(@RequestParam String fname) throws IOException {
        if (!VideoStorage.isValidName(fname)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Path uploaded = VideoStorage.directory().resolve(fname);
        Resource resource = Files.isRegularFile(uploaded) ? new FileSystemResource(uploaded)
                : context.getResource("/videos/" + fname);
        if (!resource.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(VideoStorage.contentType(fname)))
                .body(resource);
    }
}
