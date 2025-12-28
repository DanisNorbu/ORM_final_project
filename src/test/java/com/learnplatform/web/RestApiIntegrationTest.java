package com.learnplatform.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learnplatform.entity.QuestionType;
import com.learnplatform.entity.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestApiIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @Test
    void userCrud_roundTrip() throws Exception {
        long userId = createUser("Alice", "alice@example.com", UserRole.STUDENT);

        // read
        JsonNode got = readJson(get("/api/users/{id}", userId));
        assertThat(got.get("id").asLong()).isEqualTo(userId);

        // patch
        String patchBody = om.writeValueAsString(Map.of("name", "Alice Updated"));
        JsonNode patched = readJson(patch("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchBody));
        assertThat(patched.get("name").asText()).isEqualTo("Alice Updated");

        // delete
        mvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void courseStructure_createModuleLesson_andFetchDetails() throws Exception {
        long categoryId = createCategory("Programming");
        long teacherId = createUser("Teacher", "teacher@example.com", UserRole.TEACHER);

        long courseId = createCourse("Java", categoryId, teacherId, Set.of("java", "hibernate"));
        long moduleId = addModule(courseId, "Module 1", 1);
        long lessonId = addLesson(moduleId, "Lesson 1");

        JsonNode details = readJson(get("/api/courses/{id}", courseId));
        assertThat(details.get("modules").size()).isEqualTo(1);
        assertThat(details.get("modules").get(0).get("lessons").size()).isEqualTo(1);
        assertThat(details.get("modules").get(0).get("lessons").get(0).get("id").asLong()).isEqualTo(lessonId);
        assertThat(details.get("tags").size()).isEqualTo(2);
    }

    @Test
    void enrollment_preventsDuplicates_andAllowsUnenroll() throws Exception {
        long categoryId = createCategory("Databases");
        long teacherId = createUser("Teacher2", "t2@example.com", UserRole.TEACHER);
        long studentId = createUser("Student", "student@example.com", UserRole.STUDENT);
        long courseId = createCourse("SQL", categoryId, teacherId, Set.of());

        mvc.perform(post("/api/courses/{courseId}/enroll?userId={userId}", courseId, studentId))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/courses/{courseId}/enroll?userId={userId}", courseId, studentId))
                .andExpect(status().isConflict());

        JsonNode list = readJson(get("/api/courses/{courseId}/enrollments", courseId));
        assertThat(list.size()).isEqualTo(1);

        mvc.perform(delete("/api/courses/{courseId}/enroll?userId={userId}", courseId, studentId))
                .andExpect(status().isNoContent());
    }

    @Test
    void assignment_submission_preventsDuplicates_andSupportsGrading() throws Exception {
        long categoryId = createCategory("Backend");
        long teacherId = createUser("Teacher3", "t3@example.com", UserRole.TEACHER);
        long studentId = createUser("Student3", "s3@example.com", UserRole.STUDENT);
        long courseId = createCourse("Spring", categoryId, teacherId, Set.of());
        long moduleId = addModule(courseId, "Module", 1);
        long lessonId = addLesson(moduleId, "Lesson");

        long assignmentId = createAssignment(lessonId, "HW", LocalDate.now().plusDays(7));

        long submissionId = submitAssignment(assignmentId, studentId, "my answer");

        mvc.perform(post("/api/assignments/{id}/submissions", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("studentId", studentId, "content", "second"))))
                .andExpect(status().isConflict());

        JsonNode graded = readJson(patch("/api/submissions/{id}/grade", submissionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("score", 100, "feedback", "ok"))));
        assertThat(graded.get("score").asInt()).isEqualTo(100);

        JsonNode byStudent = readJson(get("/api/users/{id}/submissions", studentId));
        assertThat(byStudent.size()).isEqualTo(1);
    }

    @Test
    void quiz_create_and_take_persistsResult() throws Exception {
        long categoryId = createCategory("QA");
        long teacherId = createUser("Teacher4", "t4@example.com", UserRole.TEACHER);
        long studentId = createUser("Student4", "s4@example.com", UserRole.STUDENT);
        long courseId = createCourse("Testing", categoryId, teacherId, Set.of());
        long moduleId = addModule(courseId, "Module", 1);

        // create quiz with one SINGLE_CHOICE question
        String quizBody = om.writeValueAsString(Map.of(
                "moduleId", moduleId,
                "title", "Quiz 1",
                "timeLimit", 10,
                "questions", List.of(Map.of(
                        "text", "2+2?",
                        "type", QuestionType.SINGLE_CHOICE,
                        "options", List.of(
                                Map.of("text", "4", "isCorrect", true),
                                Map.of("text", "5", "isCorrect", false)
                        )
                ))
        ));

        JsonNode quiz = readJson(post("/api/quizzes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(quizBody));
        long quizId = quiz.get("id").asLong();

        long questionId = quiz.get("questions").get(0).get("id").asLong();
        long correctOptionId = quiz.get("questions").get(0).get("options").get(0).get("id").asLong();

        String takeBody = om.writeValueAsString(Map.of(
                "studentId", studentId,
                "answers", Map.of(String.valueOf(questionId), List.of(correctOptionId))
        ));

        JsonNode result = readJson(post("/api/quizzes/{id}/take", quizId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(takeBody));
        assertThat(result.get("score").asInt()).isEqualTo(1);

        JsonNode resultsByQuiz = readJson(get("/api/quizzes/{id}/results", quizId));
        assertThat(resultsByQuiz.size()).isEqualTo(1);
    }

    @Test
    void validation_invalidEmail_returns400() throws Exception {
        String body = om.writeValueAsString(Map.of(
                "name", "Bob",
                "email", "not-an-email",
                "role", UserRole.STUDENT
        ));

        MvcResult res = mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode json = om.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("validationErrors").isArray()).isTrue();
    }

    @Test
    void tagCrud_roundTrip() throws Exception {
        JsonNode created = readJson(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("name", "JPA"))));
        long tagId = created.get("id").asLong();

        JsonNode list = readJson(get("/api/tags"));
        assertThat(list.size()).isGreaterThanOrEqualTo(1);

        JsonNode patched = readJson(patch("/api/tags/{id}", tagId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("name", "JPA-2"))));
        assertThat(patched.get("name").asText()).isEqualTo("JPA-2");

        mvc.perform(delete("/api/tags/{id}", tagId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/tags/{id}", tagId))
                .andExpect(status().isNotFound());
    }

    @Test
    void reviews_create_list_patch_delete() throws Exception {
        long categoryId = createCategory("Reviews");
        long teacherId = createUser("TeacherR", "teach-r@example.com", UserRole.TEACHER);
        long studentId = createUser("StudentR", "stud-r@example.com", UserRole.STUDENT);
        long courseId = createCourse("CourseR", categoryId, teacherId, Set.of());

        JsonNode review = readJson(post("/api/courses/{courseId}/reviews", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of(
                        "studentId", studentId,
                        "rating", 5,
                        "comment", "Nice"
                ))));
        long reviewId = review.get("id").asLong();

        // duplicate review is not allowed
        mvc.perform(post("/api/courses/{courseId}/reviews", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of(
                                "studentId", studentId,
                                "rating", 4,
                                "comment", "Duplicate"
                        ))))
                .andExpect(status().isConflict());

        JsonNode byCourse = readJson(get("/api/courses/{courseId}/reviews", courseId));
        assertThat(byCourse.size()).isEqualTo(1);

        JsonNode byStudent = readJson(get("/api/users/{studentId}/reviews", studentId));
        assertThat(byStudent.size()).isEqualTo(1);

        JsonNode patched = readJson(patch("/api/reviews/{id}", reviewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("comment", "Updated"))));
        assertThat(patched.get("comment").asText()).isEqualTo("Updated");

        mvc.perform(delete("/api/reviews/{id}", reviewId))
                .andExpect(status().isNoContent());
    }

    // ---------- helpers ----------

    private long createCategory(String name) throws Exception {
        String body = om.writeValueAsString(Map.of("name", name));
        JsonNode json = readJson(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        return json.get("id").asLong();
    }

    private long createUser(String name, String email, UserRole role) throws Exception {
        String body = om.writeValueAsString(Map.of("name", name, "email", email, "role", role));
        JsonNode json = readJson(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        return json.get("id").asLong();
    }

    private long createCourse(String title, long categoryId, long teacherId, Set<String> tags) throws Exception {
        String body = om.writeValueAsString(Map.of(
                "title", title,
                "description", "desc",
                "categoryId", categoryId,
                "teacherId", teacherId,
                "duration", "4 weeks",
                "startDate", LocalDate.now().toString(),
                "tagNames", tags
        ));
        JsonNode json = readJson(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        return json.get("id").asLong();
    }

    private long addModule(long courseId, String title, int orderIndex) throws Exception {
        String body = om.writeValueAsString(Map.of("title", title, "orderIndex", orderIndex, "description", "d"));
        JsonNode json = readJson(post("/api/courses/{id}/modules", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        return json.get("id").asLong();
    }

    private long addLesson(long moduleId, String title) throws Exception {
        String body = om.writeValueAsString(Map.of("title", title, "content", "c", "videoUrl", ""));
        JsonNode json = readJson(post("/api/courses/modules/{id}/lessons", moduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        return json.get("id").asLong();
    }

    private long createAssignment(long lessonId, String title, LocalDate dueDate) throws Exception {
        String body = om.writeValueAsString(Map.of("title", title, "description", "d", "dueDate", dueDate.toString(), "maxScore", 100));
        JsonNode json = readJson(post("/api/lessons/{id}/assignments", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        return json.get("id").asLong();
    }

    private long submitAssignment(long assignmentId, long studentId, String content) throws Exception {
        String body = om.writeValueAsString(Map.of("studentId", studentId, "content", content));
        JsonNode json = readJson(post("/api/assignments/{id}/submissions", assignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
        return json.get("id").asLong();
    }

    private JsonNode readJson(org.springframework.test.web.servlet.RequestBuilder rb) throws Exception {
        MvcResult res = mvc.perform(rb)
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }
}
