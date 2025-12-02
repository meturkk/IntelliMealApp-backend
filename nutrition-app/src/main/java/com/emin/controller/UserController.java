package com.emin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.emin.dto.DtoUser;
import com.emin.services.UserService;

@RestController
@RequestMapping("/rest/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    // Create

    @PostMapping
    public ResponseEntity<DtoUser> createUser(@RequestBody DtoUser dtoUser) {
        DtoUser createdUser = userService.createUser(dtoUser);
        // Başarılı oluşturma için 201 Created döndürür
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED); 
    }

    
    // Read
    
    @GetMapping("/{id}")
    public ResponseEntity<DtoUser> getUserById(@PathVariable(name = "id") String id) {
        DtoUser user = userService.getUserById(id);
        return ResponseEntity.ok(user); // Başarılıysa 200 OK döndürür
    }

    // Update

    @PutMapping("/{id}")
    public ResponseEntity<DtoUser> updateUser(@PathVariable(name = "id") String id, @RequestBody DtoUser dtoUser) {
        DtoUser updatedUser = userService.updateUser(id, dtoUser);
        return ResponseEntity.ok(updatedUser); // Başarılıysa 200 OK döndürür
    }

    
    // Delete
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") String id) {
        userService.deleteUser(id);
        return new ResponseEntity<>("User successfully deleted.", HttpStatus.NO_CONTENT); 
    }
}