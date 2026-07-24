package com.example.WorkTopus.projects.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BoardNotFoundException extends IllegalArgumentException {

    public BoardNotFoundException(String message) {
        super(message);
    }
}