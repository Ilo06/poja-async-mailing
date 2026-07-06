package api.poja.app.endpoint.rest.model;

import java.time.Instant;

public record CreateCourseRequest(String title, Instant startDate, Instant endDate) {}
