package com.emin.entities;
import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personal_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfo {

    @Id
    @UuidGenerator
    private String id;
    
    @Column(name = "date", nullable = true) 
    private LocalDateTime date;

    @Column(name = "age")
    private Integer age; 

    @Column(name = "gender", length = 50)
    private String gender; 

    @Column(name = "height")
    private Integer height; 

    @Column(name = "weight")
    private Double weight; 

    @Column(name = "target_weight", nullable = true)
    private Double targetWeight;

    @Column(name = "activity_level", length = 30)
    private String activityLevel; 

    @Column(name = "dietary_preference", length = 50)
    private String dietaryPreference; 

    @Column(name = "goal", length = 50)
    private String goal; 

    @Column(name = "health_condition", length = 50)
    private String healthCondition; 
    
    @Column(name = "neck_size", nullable = true)
    private Integer neckSize;

    @Column(name = "waist_size", nullable = true)
    private Integer waistSize;

    @Column(name = "hip_size", nullable = true)
    private Integer hipSize;

    @Column(name = "chest_size", nullable = true)
    private Integer chestSize; 

    @Column(name = "arm_size", nullable = true)
    private Integer armSize;

    @Column(name = "leg_size", nullable = true)
    private Integer legSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
