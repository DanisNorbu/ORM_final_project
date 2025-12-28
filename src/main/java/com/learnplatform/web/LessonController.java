package com.learnplatform.web;

import com.learnplatform.entity.Lesson;
import com.learnplatform.service.LessonService;
import com.learnplatform.web.dto.LessonPatchRequest;
import com.learnplatform.web.dto.LessonView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lessons")
@Validated
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/{id}")
    public LessonView get(@PathVariable("id") @Positive Long id) {
        return toView(lessonService.getOrThrow(id));
    }

    @PatchMapping("/{id}")
    public LessonView patch(@PathVariable("id") @Positive Long id, @Valid @RequestBody LessonPatchRequest req) {
        Lesson updated = lessonService.update(id, req.title(), req.content(), req.videoUrl());
        return toView(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @Positive Long id) {
        lessonService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static LessonView toView(Lesson l) {
        return new LessonView(l.getId(), l.getModule().getId(), l.getTitle(), l.getContent(), l.getVideoUrl());
    }
}
