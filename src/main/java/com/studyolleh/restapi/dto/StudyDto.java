package com.studyolleh.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class StudyDto {
    private Long id;
    private String path;
    private String title;
    private String shortDescription;
}
