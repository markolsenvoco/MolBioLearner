package com.molbiolearner.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class LessonController {

    /** Returns the curriculum index (all modules and lesson metadata). */
    @GetMapping(value = "/modules", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getModules() throws IOException {
        return jsonResource("static/content/modules.json");
    }

    /** Returns full lesson content including quiz questions. */
    @GetMapping(value = "/lessons/{moduleId}/{lessonId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getLesson(@PathVariable String moduleId,
                                             @PathVariable String lessonId) throws IOException {
        // Sanitise path segments to prevent directory traversal
        if (!moduleId.matches("[a-z0-9-]+") || !lessonId.matches("[a-z0-9-]+")) {
            return ResponseEntity.badRequest().build();
        }
        return jsonResource("static/content/" + moduleId + "/" + lessonId + ".json");
    }

    private ResponseEntity<String> jsonResource(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        return ResponseEntity.ok(content);
    }
}
