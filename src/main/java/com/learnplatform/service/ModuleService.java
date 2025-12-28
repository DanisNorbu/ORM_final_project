package com.learnplatform.service;

import com.learnplatform.entity.Module;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.ModuleRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    public ModuleService(
            ModuleRepository moduleRepository,
            SubmissionRepository submissionRepository,
            QuizSubmissionRepository quizSubmissionRepository
    ) {
        this.moduleRepository = moduleRepository;
        this.submissionRepository = submissionRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
    }

    @Transactional(readOnly = true)
    public Module getOrThrow(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found: id=" + moduleId));
    }

    @Transactional(readOnly = true)
    public Module getWithLessonsOrThrow(Long moduleId) {
        return moduleRepository.findWithLessonsById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found: id=" + moduleId));
    }

    @Transactional
    public Module update(Long moduleId, String title, Integer orderIndex, String description) {
        Module module = getOrThrow(moduleId);
        if (title != null) module.setTitle(title);
        if (orderIndex != null) module.setOrderIndex(orderIndex);
        if (description != null) module.setDescription(description);

        // Initialize lessons so controller can safely serialize a ModuleView with lessons (open-in-view is disabled).
        module.getCourse().getId();
        module.getLessons().size();
        return moduleRepository.save(module);
    }

    @Transactional
    public void delete(Long moduleId) {
        Module module = getOrThrow(moduleId);

        boolean hasSubmissions = submissionRepository.existsByModuleId(moduleId);
        boolean hasQuizResults = quizSubmissionRepository.existsByModuleId(moduleId);
        if (hasSubmissions || hasQuizResults) {
            throw new ConflictException("Cannot delete module id=" + moduleId + " because it has learning data");
        }

        moduleRepository.delete(module);
    }
}
