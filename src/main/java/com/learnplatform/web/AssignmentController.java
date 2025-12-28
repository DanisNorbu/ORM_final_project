package com.learnplatform.web;

import com.learnplatform.entity.Assignment;
import com.learnplatform.entity.Submission;
import com.learnplatform.service.AssignmentService;
import com.learnplatform.web.dto.AssignmentCreateRequest;
import com.learnplatform.web.dto.AssignmentPatchRequest;
import com.learnplatform.web.dto.AssignmentView;
import com.learnplatform.web.dto.SubmissionCreateRequest;
import com.learnplatform.web.dto.SubmissionGradeRequest;
import com.learnplatform.web.dto.SubmissionView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Validated
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/lessons/{lessonId}/assignments")
    public ResponseEntity<AssignmentView> createAssignment(
            @PathVariable @Positive Long lessonId,
            @Valid @RequestBody AssignmentCreateRequest req
    ) {
        Assignment created = assignmentService.createAssignment(
                lessonId,
                req.title(),
                req.description(),
                req.dueDate(),
                req.maxScore()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(created));
    }

    @GetMapping("/assignments/{assignmentId}")
    public AssignmentView getAssignment(@PathVariable @Positive Long assignmentId) {
        return toView(assignmentService.getAssignmentOrThrow(assignmentId));
    }

    @PatchMapping("/assignments/{assignmentId}")
    public AssignmentView patchAssignment(
            @PathVariable @Positive Long assignmentId,
            @Valid @RequestBody AssignmentPatchRequest req
    ) {
        Assignment updated = assignmentService.updateAssignment(
                assignmentId,
                req.title(),
                req.description(),
                req.dueDate(),
                req.maxScore()
        );
        return toView(updated);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable @Positive Long assignmentId) {
        assignmentService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<SubmissionView> submit(
            @PathVariable @Positive Long assignmentId,
            @Valid @RequestBody SubmissionCreateRequest req
    ) {
        Submission created = assignmentService.submit(assignmentId, req.studentId(), req.content());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toView(created));
    }

    @PatchMapping("/submissions/{submissionId}/grade")
    public SubmissionView grade(
            @PathVariable @Positive Long submissionId,
            @Valid @RequestBody SubmissionGradeRequest req
    ) {
        Submission updated = assignmentService.grade(submissionId, req.score(), req.feedback());
        return toView(updated);
    }

    @DeleteMapping("/submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable @Positive Long submissionId) {
        assignmentService.deleteSubmission(submissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public List<SubmissionView> listByAssignment(@PathVariable @Positive Long assignmentId) {
        return assignmentService.listSubmissionsByAssignment(assignmentId).stream()
                .map(AssignmentController::toView)
                .toList();
    }

    @GetMapping("/users/{studentId}/submissions")
    public List<SubmissionView> listByStudent(@PathVariable @Positive Long studentId) {
        return assignmentService.listSubmissionsByStudent(studentId).stream()
                .map(AssignmentController::toView)
                .toList();
    }

    private static AssignmentView toView(Assignment a) {
        return new AssignmentView(
                a.getId(),
                a.getLesson().getId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueDate(),
                a.getMaxScore()
        );
    }

    private static SubmissionView toView(Submission s) {
        return new SubmissionView(
                s.getId(),
                s.getAssignment().getId(),
                s.getStudent().getId(),
                s.getSubmittedAt(),
                s.getContent(),
                s.getScore(),
                s.getFeedback()
        );
    }
}
