package com.emin.controller;

import com.emin.dto.DtoPersonalInfo;
import com.emin.exceptions.ResourceNotFoundException;
import com.emin.services.PersonalInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/users/{userId}/personal-info") 
public class PersonalInfoController {

    @Autowired
    private PersonalInfoService personalInfoService;

    // Create
    
    @PostMapping
    public ResponseEntity<DtoPersonalInfo> createPersonalInfo(
            @PathVariable String userId,
            @RequestBody DtoPersonalInfo dtoPersonalInfo) {
        
        DtoPersonalInfo createdInfo = personalInfoService.createPersonalInfo(userId, dtoPersonalInfo);
        return new ResponseEntity<>(createdInfo, HttpStatus.CREATED);
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<DtoPersonalInfo>> getAllPersonalInfoForUser(@PathVariable String userId) {
        List<DtoPersonalInfo> infoList = personalInfoService.getAllByUserId(userId);
        return ResponseEntity.ok(infoList);
    }
    
    // Read One

    @GetMapping("/{infoId}")
    public ResponseEntity<DtoPersonalInfo> getPersonalInfoById(@PathVariable String userId, @PathVariable String infoId) {
        List<DtoPersonalInfo> infoList = personalInfoService.getAllByUserId(userId);

        DtoPersonalInfo dtoInfo = infoList.stream()
            .filter(i -> i != null && i.getId() != null && i.getId().equals(infoId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("PersonalInfo", "id", infoId));

        return ResponseEntity.ok(dtoInfo);
    }

    
    // Update
    

    @PutMapping("/{infoId}")
    public ResponseEntity<DtoPersonalInfo> updatePersonalInfo(
            @PathVariable String infoId, 
            @RequestBody DtoPersonalInfo dtoPersonalInfo) {
        
        DtoPersonalInfo updatedInfo = personalInfoService.updatePersonalInfo(infoId, dtoPersonalInfo);
        return ResponseEntity.ok(updatedInfo);
    }

    // Delete
    
    @DeleteMapping("/{infoId}")
    public ResponseEntity<Void> deletePersonalInfo(@PathVariable String infoId) {
        personalInfoService.deletePersonalInfo(infoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); 
    }
}