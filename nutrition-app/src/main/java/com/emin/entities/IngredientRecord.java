package com.emin.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ingredient_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRecord {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_record_id", nullable = false)
    private MealRecord mealRecord; 

    @Column(name = "ingredient_string", length = 255)
    private String ingredientString; 
}