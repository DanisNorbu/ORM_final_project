package com.learnplatform.web;

import com.learnplatform.entity.Enrollment;
import com.learnplatform.service.EnrollmentService;
import com.learnplatform.web.dto.EnrollmentView;
import jakarta.validation.constraints.Positive;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Validated
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<EnrollmentView> enroll(
            @PathVariable @Positive Long courseId,
            @RequestParam("userId") @Positive Long userId
    ) {
        Enrollment created = enrollmentService.enroll(userId, courseId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/enrollments/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(created));
    }

    @DeleteMapping("/courses/{courseId}/enroll")
    public ResponseEntity<Void> unenroll(
            @PathVariable @Positive Long courseId,
            @RequestParam("userId") @Positive Long userId
    ) {
        enrollmentService.unenroll(userId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/enrollments")
    public List<EnrollmentView> listByCourse(@PathVariable @Positive Long courseId) {
        return enrollmentService.listByCourse(courseId).stream().map(EnrollmentController::toView).toList();
    }

    @GetMapping("/users/{userId}/enrollments")
    public List<EnrollmentView> listByUser(@PathVariable @Positive Long userId) {
        return enrollmentService.listByUser(userId).stream().map(EnrollmentController::toView).toList();
    }

    private static EnrollmentView toView(Enrollment e) {
        return new EnrollmentView(
                e.getId(),
                e.getUser().getId(),
                e.getCourse().getId(),
                e.getEnrollDate(),
                e.getStatus()
        );
    }
}
