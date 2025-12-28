package com.learnplatform.web;

import com.learnplatform.entity.CourseReview;
import com.learnplatform.service.CourseReviewService;
import com.learnplatform.web.dto.CourseReviewCreateRequest;
import com.learnplatform.web.dto.CourseReviewPatchRequest;
import com.learnplatform.web.dto.CourseReviewView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class CourseReviewController {

    private final CourseReviewService reviewService;

    public CourseReviewController(CourseReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/api/courses/{courseId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseReviewView create(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody CourseReviewCreateRequest req
    ) {
        CourseReview review = reviewService.create(courseId, req.studentId(), req.rating(), req.comment());
        return toView(review);
    }

    @GetMapping("/api/courses/{courseId}/reviews")
    public List<CourseReviewView> listByCourse(@PathVariable @Positive Long courseId) {
        return reviewService.listByCourse(courseId).stream().map(CourseReviewController::toView).toList();
    }

    @GetMapping("/api/users/{studentId}/reviews")
    public List<CourseReviewView> listByStudent(@PathVariable @Positive Long studentId) {
        return reviewService.listByStudent(studentId).stream().map(CourseReviewController::toView).toList();
    }

    @PatchMapping("/api/reviews/{reviewId}")
    public CourseReviewView patch(
            @PathVariable @Positive Long reviewId,
            @Valid @RequestBody CourseReviewPatchRequest req
    ) {
        return toView(reviewService.update(reviewId, req.rating(), req.comment()));
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long reviewId) {
        reviewService.delete(reviewId);
    }

    private static CourseReviewView toView(CourseReview r) {
        return new CourseReviewView(
                r.getId(),
                r.getCourse().getId(),
                r.getStudent().getId(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
