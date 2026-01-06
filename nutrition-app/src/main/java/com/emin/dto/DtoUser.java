package com.emin.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoUser {
    
    private String id;
    private String name;    
    private String surname;
    private String phoneNumber;
    private String email;
    private String password;
    private String role;
    private boolean verified;
    private List<DtoPersonalInfo> personalInfo;
}
