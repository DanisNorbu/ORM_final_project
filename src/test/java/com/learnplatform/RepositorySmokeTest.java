package com.learnplatform;

import com.learnplatform.entity.*;
import com.learnplatform.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RepositorySmokeTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    ModuleRepository moduleRepository;
    @Autowired
    LessonRepository lessonRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    @Transactional
    void saveAndLoad_basicGraph_worksInsideTransaction() {
        User teacher = userRepository.save(new User("Teacher", "t@example.com", UserRole.TEACHER));
        User student = userRepository.save(new User("Student", "s@example.com", UserRole.STUDENT));

        Category cat = categoryRepository.save(new Category("Programming"));

        Tag java = tagRepository.save(new Tag("Java"));
        Tag hibernate = tagRepository.save(new Tag("Hibernate"));

        Course course = new Course("JPA Basics", "Intro", cat, teacher, "2 weeks", LocalDate.now());
        course.addTag(java);
        course.addTag(hibernate);

        Module m = new Module("Entities", 0, "Mapping");
        Lesson l = new Lesson("Lesson 1", "Content", null);
        m.addLesson(l);
        course.addModule(m);

        courseRepository.save(course);

        Enrollment enrollment = enrollmentRepository.save(new Enrollment(student, course, LocalDate.now(), EnrollmentStatus.ACTIVE));
        assertNotNull(enrollment.getId());

        Course loaded = courseRepository.findWithStructureById(course.getId()).orElseThrow();
        assertEquals(1, loaded.getModules().size());
        assertEquals(1, loaded.getModules().get(0).getLessons().size());
        assertEquals(Set.of("Java", "Hibernate"),
                loaded.getTags().stream().map(Tag::getName).collect(java.util.stream.Collectors.toSet()));
    }
}
