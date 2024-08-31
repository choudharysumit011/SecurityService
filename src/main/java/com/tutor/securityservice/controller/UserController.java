package com.tutor.securityservice.controller;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tutor.securityservice.dto.User;
import com.tutor.securityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor

@Slf4j
public class UserController {

    private final UserService service;

    @PostMapping("/register")
    public ResponseEntity<ObjectNode> register(
            @RequestBody User user
    ) {
       return service.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<ObjectNode> login(@RequestBody User user) {
        return service.login(user);
    }



    @GetMapping
    public List<User> findAll() {
        return service.findAll();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }
}
