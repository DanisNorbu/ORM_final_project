package com.learnplatform.web;

import com.learnplatform.entity.Lesson;
import com.learnplatform.entity.Module;
import com.learnplatform.service.ModuleService;
import com.learnplatform.web.dto.LessonView;
import com.learnplatform.web.dto.ModulePatchRequest;
import com.learnplatform.web.dto.ModuleView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;

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
@RequestMapping("/api/modules")
@Validated
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("/{id}")
    public ModuleView get(@PathVariable("id") @Positive Long id) {
        Module module = moduleService.getWithLessonsOrThrow(id);
        return toView(module);
    }

    @PatchMapping("/{id}")
    public ModuleView patch(@PathVariable("id") @Positive Long id, @Valid @RequestBody ModulePatchRequest req) {
        Module updated = moduleService.update(id, req.title(), req.orderIndex(), req.description());
        return toView(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @Positive Long id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static ModuleView toView(Module m) {
        List<LessonView> lessons = m.getLessons().stream().map(ModuleController::toView).toList();
        return new ModuleView(
                m.getId(),
                m.getCourse().getId(),
                m.getTitle(),
                m.getOrderIndex(),
                m.getDescription(),
                lessons
        );
    }

    private static LessonView toView(Lesson l) {
        return new LessonView(l.getId(), l.getModule().getId(), l.getTitle(), l.getContent(), l.getVideoUrl());
    }
}
