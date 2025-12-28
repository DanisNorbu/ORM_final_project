package com.learnplatform.service;

import com.learnplatform.entity.Lesson;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.LessonRepository;
import com.learnplatform.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SubmissionRepository submissionRepository;

    public LessonService(LessonRepository lessonRepository, SubmissionRepository submissionRepository) {
        this.lessonRepository = lessonRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public Lesson getOrThrow(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: id=" + lessonId));

        // Make module id available for serialization while open-in-view is disabled.
        lesson.getModule().getId();
        return lesson;
    }

    @Transactional
    public Lesson update(Long lessonId, String title, String content, String videoUrl) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: id=" + lessonId));

        if (title != null) lesson.setTitle(title);
        if (content != null) lesson.setContent(content);
        if (videoUrl != null) lesson.setVideoUrl(videoUrl);

        lesson.getModule().getId();
        return lessonRepository.save(lesson);
    }

    @Transactional
    public void delete(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Lesson not found: id=" + lessonId));

        boolean hasSubmissions = submissionRepository.existsByLessonId(lessonId);
        if (hasSubmissions) {
            throw new ConflictException("Cannot delete lesson id=" + lessonId + " because it has submissions");
        }

        lessonRepository.delete(lesson);
    }
}
