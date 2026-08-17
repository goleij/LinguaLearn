package com.germanlearning.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String language;

    /**
     * Stored in the existing "level" column; the previous values ("A1") are
     * exactly the enum names, so no data migration is needed.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CefrLevel level;

    @Column(length = 500)
    private String description;

    /**
     * Which revision of the authored content this course currently holds.
     *
     * Lets the seeder tell "already installed" from "installed, but an older
     * version than the code now defines", which is what decides whether a
     * course gets refreshed on start-up. Null means it predates versioning.
     */
    @Column(name = "content_version")
    private Integer contentVersion;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Unit> units = new ArrayList<>();

    public Course() {
    }

    public Course(String name, String language, CefrLevel level, String description) {
        this.name = name;
        this.language = language;
        this.level = level;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public CefrLevel getLevel() {
        return level;
    }

    public void setLevel(CefrLevel level) {
        this.level = level;
    }

    public Integer getContentVersion() {
        return contentVersion;
    }

    public void setContentVersion(Integer contentVersion) {
        this.contentVersion = contentVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    public void addUnit(Unit unit) {
        units.add(unit);
        unit.setCourse(this);
    }
}
