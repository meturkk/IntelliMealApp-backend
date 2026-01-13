package com.emin.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "meal_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealRecord {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private DailyPlan dailyPlan; 

    @Column(name = "meal_type", length = 50)
    private String mealType; 

    @Column(name = "meal_name", length = 200)
    private String mealName;

    @Column(name = "total_calories")
    private Integer totalCalories;
    
    @Column(name = "total_protein_g")
    private Double totalProteinG;

    @Column(name = "health_benefit_note", length = 500)
    private String healthBenefitNote;

    @ElementCollection
    @CollectionTable(name = "meal_ingredients", joinColumns = @JoinColumn(name = "meal_id"))
    @Column(name = "ingredient")
    private List<String> ingredients;
}