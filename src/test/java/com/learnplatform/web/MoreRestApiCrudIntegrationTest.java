package com.learnplatform.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learnplatform.entity.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
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
class MoreRestApiCrudIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @Test
    void profile_upsert_get_delete_roundTrip() throws Exception {
        long userId = createUser("Alice", "alice-profile@example.com", UserRole.STUDENT);

        JsonNode created = readJson(put("/api/users/{id}/profile", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("bio", "hello", "avatarUrl", "https://ex.com/a.png"))));
        assertThat(created.get("userId").asLong()).isEqualTo(userId);

        JsonNode got = readJson(get("/api/users/{id}/profile", userId));
        assertThat(got.get("bio").asText()).isEqualTo("hello");

        JsonNode updated = readJson(put("/api/users/{id}/profile", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("bio", "updated"))));
        assertThat(updated.get("bio").asText()).isEqualTo("updated");

        mvc.perform(delete("/api/users/{id}/profile", userId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/users/{id}/profile", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void module_and_lesson_patch_and_delete_roundTrip() throws Exception {
        long categoryId = createCategory("Programming");
        long teacherId = createUser("Teacher", "teach-ml@example.com", UserRole.TEACHER);
        long courseId = createCourse("Course", categoryId, teacherId, Set.of());

        long moduleId = addModule(courseId, "Module", 1);
        long lessonId = addLesson(moduleId, "Lesson");

        JsonNode patchedModule = readJson(patch("/api/modules/{id}", moduleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("title", "Module Updated"))));
        assertThat(patchedModule.get("title").asText()).isEqualTo("Module Updated");

        JsonNode patchedLesson = readJson(patch("/api/lessons/{id}", lessonId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("title", "Lesson Updated"))));
        assertThat(patchedLesson.get("title").asText()).isEqualTo("Lesson Updated");

        mvc.perform(delete("/api/lessons/{id}", lessonId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/lessons/{id}", lessonId))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/modules/{id}", moduleId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/modules/{id}", moduleId))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignment_patch_delete_and_deleteBlockedWhenHasSubmissions() throws Exception {
        long categoryId = createCategory("Backend");
        long teacherId = createUser("TeacherA", "teach-a@example.com", UserRole.TEACHER);
        long studentId = createUser("StudentA", "stud-a@example.com", UserRole.STUDENT);
        long courseId = createCourse("CourseA", categoryId, teacherId, Set.of());
        long moduleId = addModule(courseId, "ModuleA", 1);
        long lessonId = addLesson(moduleId, "LessonA");

        long assignmentId = createAssignment(lessonId, "HW", LocalDate.now().plusDays(7));

        JsonNode patched = readJson(patch("/api/assignments/{id}", assignmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(Map.of("title", "HW Updated"))));
        assertThat(patched.get("title").asText()).isEqualTo("HW Updated");

        // delete works when there are no submissions
        mvc.perform(delete("/api/assignments/{id}", assignmentId))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/assignments/{id}", assignmentId))
                .andExpect(status().isNotFound());

        // second assignment: deletion is blocked once submission exists
        long assignment2 = createAssignment(lessonId, "HW2", LocalDate.now().plusDays(7));
        long submissionId = submitAssignment(assignment2, studentId, "answer");

        mvc.perform(delete("/api/assignments/{id}", assignment2))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/submissions/{id}", submissionId))
                .andExpect(status().isNoContent());

        // now deletion is allowed
        mvc.perform(delete("/api/assignments/{id}", assignment2))
                .andExpect(status().isNoContent());
    }

    @Test
    void course_delete_isBlockedWhenEnrollmentsExist() throws Exception {
        long categoryId = createCategory("Databases");
        long teacherId = createUser("TeacherC", "teach-c@example.com", UserRole.TEACHER);
        long studentId = createUser("StudentC", "stud-c@example.com", UserRole.STUDENT);
        long courseId = createCourse("SQL", categoryId, teacherId, Set.of());

        mvc.perform(post("/api/courses/{courseId}/enroll?userId={userId}", courseId, studentId))
                .andExpect(status().isCreated());

        mvc.perform(delete("/api/courses/{id}", courseId))
                .andExpect(status().isConflict());
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
                "description", "d",
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
        String body = om.writeValueAsString(Map.of(
                "title", title,
                "description", "d",
                "dueDate", dueDate.toString(),
                "maxScore", 100
        ));
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
