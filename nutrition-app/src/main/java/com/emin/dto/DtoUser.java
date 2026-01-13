package com.emin.dto;


import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DtoUser {
    
    private String id;

    @NotBlank(message = "Name cannot be empty")
    private String name;    

    @NotBlank(message = "Surname cannot be empty")
    private String surname;

    private String phoneNumber;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String role;
    private Boolean verified;
    private Integer isReceived;
    private List<DtoPersonalInfo> personalInfo;
}
