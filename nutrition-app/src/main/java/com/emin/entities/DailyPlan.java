package com.emin.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;

@Entity
@Table(name = "daily_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlan {

    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; 

    @Column(name = "day_number")
    private Integer day; 

    @Column(name = "total_calories")
    private Integer totalCalories; 
    
    @Column(name = "total_protein")
    private Double totalProteinG; 

    @Column(name = "is_checked")
    private Boolean checked = false;

    // Günlük plana ait tüm öğün kayıtları
    @OneToMany(mappedBy = "dailyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealRecord> mealRecords; 
}