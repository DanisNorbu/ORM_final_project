package com.learnplatform.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false, foreignKey = @ForeignKey(name = "fk_qsub_quiz"))
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, foreignKey = @ForeignKey(name = "fk_qsub_student"))
    private User student;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private OffsetDateTime takenAt;

    protected QuizSubmission() {
    }

    public QuizSubmission(Quiz quiz, User student, Integer score, OffsetDateTime takenAt) {
        this.quiz = quiz;
        this.student = student;
        this.score = score;
        this.takenAt = takenAt;
    }

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public User getStudent() {
        return student;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public OffsetDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(OffsetDateTime takenAt) {
        this.takenAt = takenAt;
    }
}
