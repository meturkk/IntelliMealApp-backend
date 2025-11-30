package com.emin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emin.dto.DtoUser;
import com.emin.services.UserService;

@RestController
@RequestMapping("/rest/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @GetMapping(path="/{id}")
    public DtoUser getUserById(@PathVariable(name = "id") String id) {
        return userService.getUserById(id);
    }
}
