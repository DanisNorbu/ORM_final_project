package com.learnplatform.service;

import com.learnplatform.entity.Category;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.Lesson;
import com.learnplatform.entity.Module;
import com.learnplatform.entity.Tag;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.CategoryRepository;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.CourseReviewRepository;
import com.learnplatform.repository.EnrollmentRepository;
import com.learnplatform.repository.LessonRepository;
import com.learnplatform.repository.ModuleRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.SubmissionRepository;
import com.learnplatform.repository.TagRepository;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.service.dto.CourseCreateCommand;
import com.learnplatform.service.dto.CourseUpdateCommand;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;

    public CourseService(
            CourseRepository courseRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            TagRepository tagRepository,
            ModuleRepository moduleRepository,
            LessonRepository lessonRepository,
            EnrollmentRepository enrollmentRepository,
            SubmissionRepository submissionRepository,
            QuizSubmissionRepository quizSubmissionRepository,
            CourseReviewRepository courseReviewRepository
    ) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.submissionRepository = submissionRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.courseReviewRepository = courseReviewRepository;
    }

    @Transactional
    public Course create(CourseCreateCommand cmd) {
        Category category = categoryRepository.findById(cmd.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + cmd.categoryId()));

        User teacher = userRepository.findById(cmd.teacherId())
                .orElseThrow(() -> new NotFoundException("Teacher not found: id=" + cmd.teacherId()));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new ConflictException("User id=" + teacher.getId() + " is not a TEACHER");
        }

        Course course = new Course(
                cmd.title(),
                cmd.description(),
                category,
                teacher,
                cmd.duration(),
                cmd.startDate()
        );

        // Tags are optional; create missing tags.
        Set<String> tagNames = cmd.tagNames();
        if (tagNames != null) {
            for (String rawName : tagNames) {
                if (rawName == null || rawName.isBlank()) continue;
                String name = rawName.trim();
                Tag tag = tagRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name)));
                course.addTag(tag);
            }
        }

        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public Course getOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public Course getWithStructureOrThrow(Long id) {
        return courseRepository.findWithStructureById(id)
                .orElseThrow(() -> new NotFoundException("Course not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Course> list() {
        return courseRepository.findAll();
    }

    @Transactional
    public Course update(Long id, CourseUpdateCommand cmd) {
        Course course = getOrThrow(id);

        if (cmd.title() != null) course.setTitle(cmd.title());
        if (cmd.description() != null) course.setDescription(cmd.description());
        if (cmd.duration() != null) course.setDuration(cmd.duration());
        if (cmd.startDate() != null) course.setStartDate(cmd.startDate());

        if (cmd.categoryId() != null) {
            Category category = categoryRepository.findById(cmd.categoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found: id=" + cmd.categoryId()));
            course.setCategory(category);
        }

        if (cmd.teacherId() != null) {
            User teacher = userRepository.findById(cmd.teacherId())
                    .orElseThrow(() -> new NotFoundException("Teacher not found: id=" + cmd.teacherId()));
            if (teacher.getRole() != UserRole.TEACHER) {
                throw new ConflictException("User id=" + teacher.getId() + " is not a TEACHER");
            }
            course.setTeacher(teacher);
        }

        if (cmd.tagNames() != null) {
            course.getTags().clear();
            for (String rawName : cmd.tagNames()) {
                if (rawName == null || rawName.isBlank()) continue;
                String normalized = rawName.trim();
                Tag tag = tagRepository.findByNameIgnoreCase(normalized)
                        .orElseGet(() -> tagRepository.save(new Tag(normalized)));
                course.addTag(tag);
            }
        }

        return courseRepository.save(course);
    }

    @Transactional
    public Module addModule(Long courseId, String title, int orderIndex, String description) {
        Course course = getOrThrow(courseId);
        Module module = new Module(title, orderIndex, description);
        course.addModule(module);
        courseRepository.save(course);
        return module;
    }

    @Transactional
    public Lesson addLesson(Long moduleId, String title, String content, String videoUrl) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found: id=" + moduleId));
        Lesson lesson = new Lesson(title, content, videoUrl);
        module.addLesson(lesson);
        moduleRepository.save(module);
        return lesson;
    }

    /**
     * Conservative delete used for later iterations: do not allow deletion if learning process has started.
     */
    @Transactional
    public void delete(Long courseId) {
        Course course = getOrThrow(courseId);

        boolean hasEnrollments = enrollmentRepository.existsByCourseId(courseId);
        boolean hasSubmissions = submissionRepository.existsByCourseId(courseId);
        boolean hasQuizResults = quizSubmissionRepository.existsByCourseId(courseId);
        boolean hasReviews = courseReviewRepository.existsByCourseId(courseId);
        if (hasEnrollments || hasSubmissions || hasQuizResults || hasReviews) {
            throw new ConflictException("Cannot delete course id=" + courseId + " because it has learning data");
        }

        courseRepository.delete(course);
    }
}
