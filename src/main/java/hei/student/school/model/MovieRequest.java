package hei.student.school.model;

import java.time.Duration;
import java.util.Set;

public record MovieRequest(
    String title, Set<Genre> genres, String description, Duration duration) {}
