package com.learnplatform.service;

import com.learnplatform.entity.User;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.CourseReviewRepository;
import com.learnplatform.repository.EnrollmentRepository;
import com.learnplatform.repository.ProfileRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.SubmissionRepository;
import com.learnplatform.repository.UserRepository;
import com.learnplatform.service.dto.UserCreateCommand;
import com.learnplatform.service.dto.UserUpdateCommand;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseReviewRepository courseReviewRepository;

    public UserService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            SubmissionRepository submissionRepository,
            QuizSubmissionRepository quizSubmissionRepository,
            CourseReviewRepository courseReviewRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.submissionRepository = submissionRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.courseReviewRepository = courseReviewRepository;
    }

    @Transactional
    public User create(UserCreateCommand cmd) {
        if (userRepository.existsByEmail(cmd.email())) {
            throw new ConflictException("Email already exists: " + cmd.email());
        }
        User user = new User(cmd.name(), cmd.email(), cmd.role());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        return userRepository.findAll();
    }

    @Transactional
    public User update(Long id, UserUpdateCommand cmd) {
        User user = getOrThrow(id);

        if (cmd.email() != null && !cmd.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(cmd.email())) {
                throw new ConflictException("Email already exists: " + cmd.email());
            }
            user.setEmail(cmd.email());
        }

        if (cmd.name() != null) {
            user.setName(cmd.name());
        }
        if (cmd.role() != null) {
            user.setRole(cmd.role());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getOrThrow(id);

        // Guard rails: prevent deleting users that are referenced by learning process.
        boolean hasProfile = profileRepository.existsByUserId(id);
        boolean teaches = courseRepository.existsByTeacherId(id);
        boolean enrolled = enrollmentRepository.existsByUserId(id);
        boolean hasSubmissions = submissionRepository.existsByStudentId(id);
        boolean hasQuizResults = quizSubmissionRepository.existsByStudentId(id);
        boolean hasReviews = courseReviewRepository.existsByStudentId(id);

        if (hasProfile || teaches || enrolled || hasSubmissions || hasQuizResults || hasReviews) {
            throw new ConflictException(
                    "Cannot delete user id=" + id + " because it is referenced by other entities"
            );
        }

        userRepository.delete(user);
    }
}
