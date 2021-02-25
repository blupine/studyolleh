package com.studyolleh.restapi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private HttpStatus httpStatus;

    private String message;

    public static ErrorResponse CreateErrorResponse(HttpStatus httpStatus, String message) {
        return new ErrorResponse(httpStatus, message);
    }
}
