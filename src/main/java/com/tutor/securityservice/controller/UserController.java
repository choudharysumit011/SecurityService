package com.tutor.securityservice.controller;


import com.tutor.securityservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public String getAllUsers() {
        log.info("getAllUsers");
        System.out.println("User endpoint");
        return"Hello World";

    }

}
