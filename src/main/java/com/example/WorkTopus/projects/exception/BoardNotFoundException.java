package com.example.WorkTopus.projects.exception;

<<<<<<< HEAD
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BoardNotFoundException extends IllegalArgumentException {

    public BoardNotFoundException(String message) {
        super(message);
    }
=======
public class BoardNotFoundException {
>>>>>>> origin/feature/admin
}
