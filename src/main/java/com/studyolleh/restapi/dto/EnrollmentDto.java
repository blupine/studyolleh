package com.studyolleh.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor @NoArgsConstructor
public class EnrollmentDto {
    private Long id;
//    private EventDto event;
    private LocalDateTime enrolledAt;
    private boolean accepted;
    private boolean attended;
}