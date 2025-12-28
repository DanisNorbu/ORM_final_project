package com.learnplatform;

import com.learnplatform.entity.Category;
import com.learnplatform.entity.Course;
import com.learnplatform.entity.Lesson;
import com.learnplatform.entity.Module;
import com.learnplatform.entity.User;
import com.learnplatform.entity.UserRole;
import com.learnplatform.repository.CategoryRepository;
import com.learnplatform.repository.CourseRepository;
import com.learnplatform.repository.UserRepository;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LazyLoadingIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void lazyCollectionAccessOutsideTransaction_throwsLazyInitializationException() {
        Long courseId = seedCourse();

        Course course = courseRepository.findById(courseId).orElseThrow();
        assertThrows(LazyInitializationException.class, () -> course.getModules().size());
    }

    @Test
    void entityGraphFetch_allowsSafeTraversalOutsideTransaction() {
        Long courseId = seedCourse();

        Course course = courseRepository.findWithStructureById(courseId).orElseThrow();
        assertDoesNotThrow(() -> course.getModules().size());
        assertEquals(1, course.getModules().size());
        assertDoesNotThrow(() -> course.getModules().get(0).getLessons().size());
        assertEquals(1, course.getModules().get(0).getLessons().size());
    }

    private Long seedCourse() {
        String suffix = String.valueOf(System.nanoTime());
        User teacher = userRepository.save(new User("Teacher", "lazy.teacher-" + suffix + "@example.com", UserRole.TEACHER));
        Category category = categoryRepository.save(new Category("LazyTest-" + suffix));
        Course course = new Course("Lazy Course", "", category, teacher, null, null);

        Module module = new Module("M1", 1, null);
        Lesson lesson = new Lesson("L1", "", null);
        module.addLesson(lesson);
        course.addModule(module);

        return courseRepository.save(course).getId();
    }
}
