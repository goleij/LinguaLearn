package com.germanlearning.service.activity;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import java.util.Set;

/**
 * Grades one family of activity types.
 *
 * A new activity type needs an implementation of this and a renderer in the
 * frontend; nothing else in the lesson flow changes.
 */
public interface ActivityGrader {

    Set<ActivityType> supportedTypes();

    GradingResult grade(LessonActivity activity, String answer);
}
