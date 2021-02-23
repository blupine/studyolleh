package com.studyolleh.modules.exception;

import com.studyolleh.restapi.account.RestAccountController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RestExceptionAdvice {

    @ExceptionHandler(value = {BadCredentialsException.class})
    public ResponseEntity  badCredentialException(HttpServletRequest req, BadCredentialsException e) {
        EntityModel<ErrorResponse> entityModel = EntityModel.of(new ErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage()));
        entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("login").withSelfRel());
        entityModel.add(WebMvcLinkBuilder.linkTo(RestAccountController.class).slash("signup").withRel("signup"));
        entityModel.add(Link.of("/docs/index.html#user-login-fail").withRel("profile"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(entityModel);
    }

}
