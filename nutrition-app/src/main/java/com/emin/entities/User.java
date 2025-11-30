package com.emin.entities;

import jakarta.annotation.Generated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    String id;

    @Column
    String name;

    @Column
    String surname;

    @Column(unique = true)
    String phoneNumber;

    @Column(unique = true)
    String email;

    @Column
    String password;    

    @Column
    String role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private PersonalInfo personalInfo;
}
