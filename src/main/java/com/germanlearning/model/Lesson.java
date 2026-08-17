package com.germanlearning.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int orderIndex;

    @Column(length = 500)
    private String description;

    /** CEFR level of this lesson; falls back to the course level when unset. */
    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level")
    private CefrLevel cefrLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<LessonActivity> activities = new ArrayList<>();

    public Lesson() {
    }

    public Lesson(String name, int orderIndex, String description) {
        this.name = name;
        this.orderIndex = orderIndex;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CefrLevel getCefrLevel() {
        return cefrLevel;
    }

    public void setCefrLevel(CefrLevel cefrLevel) {
        this.cefrLevel = cefrLevel;
    }

    /** The lesson level, or the course level when the lesson does not set one. */
    public CefrLevel resolveCefrLevel() {
        if (cefrLevel != null) {
            return cefrLevel;
        }
        return unit != null && unit.getCourse() != null ? unit.getCourse().getLevel() : null;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public List<LessonActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<LessonActivity> activities) {
        this.activities = activities;
    }

    /** Appends an activity, giving it the next free position. */
    public void addActivity(LessonActivity activity) {
        activity.setPosition(activities.size());
        activities.add(activity);
        activity.setLesson(this);
    }

    public int getTotalActivities() {
        return activities.size();
    }
}
