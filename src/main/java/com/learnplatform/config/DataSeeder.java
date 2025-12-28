package com.learnplatform.config;

import com.learnplatform.entity.AnswerOption;
import com.learnplatform.entity.Assignment;
import com.learnplatform.entity.Category;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.CourseReview;
import com.learnplatform.entity.Enrollment;
import com.learnplatform.entity.EnrollmentStatus;
import com.learnplatform.entity.Lesson;
import com.learnplatform.entity.Module;
import com.learnplatform.entity.Profile;
import com.learnplatform.entity.Question;
import com.learnplatform.entity.QuestionType;
import com.learnplatform.entity.Quiz;
import com.learnplatform.entity.QuizSubmission;
import com.learnplatform.entity.Submission;
import com.learnplatform.entity.Tag;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.repository.AssignmentRepository;
import com.learnplatform.repository.CategoryRepository;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.CourseReviewRepository;
import com.learnplatform.repository.EnrollmentRepository;
import com.learnplatform.repository.QuizSubmissionRepository;
import com.learnplatform.repository.TagRepository;
import com.learnplatform.repository.UserRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final AssignmentRepository assignmentRepository;

    public DataSeeder(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            CourseReviewRepository courseReviewRepository,
            QuizSubmissionRepository quizSubmissionRepository,
            AssignmentRepository assignmentRepository
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User teacher = new User("Dr. Hibernate", "teacher@example.com", UserRole.TEACHER);
        teacher.setProfile(new Profile("Java/Spring teacher", "https://example.com/avatar-teacher.png"));
        teacher = userRepository.save(teacher);

        User student1 = new User("Alice Student", "alice@example.com", UserRole.STUDENT);
        student1.setProfile(new Profile("Learning ORM", "https://example.com/avatar-alice.png"));
        student1 = userRepository.save(student1);

        User student2 = new User("Bob Student", "bob@example.com", UserRole.STUDENT);
        student2.setProfile(new Profile("Learning Spring", "https://example.com/avatar-bob.png"));
        student2 = userRepository.save(student2);

        Category category = categoryRepository.save(new Category("Programming"));

        Tag java = tagRepository.save(new Tag("Java"));
        Tag hibernate = tagRepository.save(new Tag("Hibernate"));
        Tag beginner = tagRepository.save(new Tag("Beginner"));

        Course course = new Course(
                "Basics of Hibernate",
                "ORM fundamentals: entities, relations, lazy loading.",
                category,
                teacher,
                "2 weeks",
                LocalDate.now().plusDays(3)
        );
        course.addTag(java);
        course.addTag(hibernate);
        course.addTag(beginner);

        Module m1 = new Module("Module 1: Entities", 1, "JPA entities, mappings, identifiers");
        Lesson l11 = new Lesson("Lesson 1.1: @Entity", "What is an entity", null);
        Lesson l12 = new Lesson("Lesson 1.2: ID strategies", "IDENTITY/SEQUENCE", null);
        m1.addLesson(l11);
        m1.addLesson(l12);

        Assignment a111 = new Assignment("HW 1: First entity", "Create a simple entity with ID", LocalDate.now().plusDays(7), 100);
        l11.addAssignment(a111);

        Quiz q1 = new Quiz("Quiz: Entities", 10);
        Question q11 = new Question("Which annotation marks a JPA entity?", QuestionType.SINGLE_CHOICE);
        q11.addOption(new AnswerOption("@Entity", true));
        q11.addOption(new AnswerOption("@Table", false));
        q11.addOption(new AnswerOption("@Column", false));
        q1.addQuestion(q11);

        Question q12 = new Question("What does LAZY loading mean?", QuestionType.SINGLE_CHOICE);
        q12.addOption(new AnswerOption("Loads relation on first access", true));
        q12.addOption(new AnswerOption("Always loads with parent", false));
        q12.addOption(new AnswerOption("Disables relations", false));
        q1.addQuestion(q12);
        m1.setQuiz(q1);

        Module m2 = new Module("Module 2: Relations", 2, "OneToMany, ManyToOne, ManyToMany");
        Lesson l21 = new Lesson("Lesson 2.1: OneToMany", "Collections and mappedBy", null);
        m2.addLesson(l21);

        course.addModule(m1);
        course.addModule(m2);

        course = courseRepository.save(course);

        enrollmentRepository.save(new Enrollment(student1, course, LocalDate.now(), EnrollmentStatus.ACTIVE));
        enrollmentRepository.save(new Enrollment(student2, course, LocalDate.now(), EnrollmentStatus.ACTIVE));

        Submission s1 = new Submission(a111, student1, OffsetDateTime.now(ZoneOffset.UTC), "My entity solution");
        s1.setScore(95);
        s1.setFeedback("Good job");
        a111.addSubmission(s1);
        assignmentRepository.save(a111);

        Quiz quizPersisted = course.getModules().get(0).getQuiz();
        quizSubmissionRepository.save(new QuizSubmission(quizPersisted, student1, 2, OffsetDateTime.now(ZoneOffset.UTC)));

        courseReviewRepository.save(new CourseReview(course, student1, 5, "Great intro course", OffsetDateTime.now(ZoneOffset.UTC)));
    }
}
