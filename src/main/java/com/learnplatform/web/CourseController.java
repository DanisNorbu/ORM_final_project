package com.learnplatform.web;

import com.learnplatform.entity.Course;
import com.learnplatform.entity.Lesson;
import com.learnplatform.entity.Module;
import com.learnplatform.entity.Tag;
import com.learnplatform.service.CourseService;
import com.learnplatform.service.dto.CourseCreateCommand;
import com.learnplatform.service.dto.CourseUpdateCommand;
import com.learnplatform.web.dto.CourseCreateRequest;
import com.learnplatform.web.dto.CourseDetailsView;
import com.learnplatform.web.dto.CoursePatchRequest;
import com.learnplatform.web.dto.CourseSummaryView;
import com.learnplatform.web.dto.LessonCreateRequest;
import com.learnplatform.web.dto.LessonView;
import com.learnplatform.web.dto.ModuleCreateRequest;
import com.learnplatform.web.dto.ModuleView;
import com.learnplatform.web.dto.TagView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/courses")
@Validated
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseSummaryView> create(@Valid @RequestBody CourseCreateRequest req) {
        Course created = courseService.create(new CourseCreateCommand(
                req.title(),
                req.description(),
                req.categoryId(),
                req.teacherId(),
                req.duration(),
                req.startDate(),
                req.tagNames()
        ));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toSummary(created));
    }

    @GetMapping
    public List<CourseSummaryView> list() {
        return courseService.list().stream().map(CourseController::toSummary).toList();
    }

    @GetMapping("/{id}")
    public CourseDetailsView get(@PathVariable @Positive Long id) {
        Course course = courseService.getWithStructureOrThrow(id);
        return toDetails(course);
    }

    @PatchMapping("/{id}")
    public CourseSummaryView patch(@PathVariable @Positive Long id, @Valid @RequestBody CoursePatchRequest req) {
        Course updated = courseService.update(id, new CourseUpdateCommand(
                req.title(),
                req.description(),
                req.categoryId(),
                req.teacherId(),
                req.duration(),
                req.startDate(),
                req.tagNames()
        ));
        return toSummary(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/modules")
    public ResponseEntity<ModuleView> addModule(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody ModuleCreateRequest req
    ) {
        Module module = courseService.addModule(courseId, req.title(), req.orderIndex(), req.description());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(module.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(module));
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<LessonView> addLesson(
            @PathVariable @Positive Long moduleId,
            @Valid @RequestBody LessonCreateRequest req
    ) {
        Lesson lesson = courseService.addLesson(moduleId, req.title(), req.content(), req.videoUrl());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(lesson.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(lesson));
    }

    private static CourseSummaryView toSummary(Course c) {
        return new CourseSummaryView(
                c.getId(),
                c.getTitle(),
                c.getCategory().getId(),
                c.getTeacher().getId(),
                c.getDuration(),
                c.getStartDate()
        );
    }

    private static CourseDetailsView toDetails(Course c) {
        List<TagView> tags = c.getTags().stream()
                .map(CourseController::toView)
                .toList();
        List<ModuleView> modules = c.getModules().stream().map(CourseController::toView).toList();

        return new CourseDetailsView(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getCategory().getId(),
                c.getTeacher().getId(),
                c.getDuration(),
                c.getStartDate(),
                tags,
                modules
        );
    }

    private static ModuleView toView(Module m) {
        List<LessonView> lessons = m.getLessons().stream().map(CourseController::toView).toList();
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
        return new LessonView(
                l.getId(),
                l.getModule().getId(),
                l.getTitle(),
                l.getContent(),
                l.getVideoUrl()
        );
    }

    private static TagView toView(Tag t) {
        return new TagView(t.getId(), t.getName());
    }
}
