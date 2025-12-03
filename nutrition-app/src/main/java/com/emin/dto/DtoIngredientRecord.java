package com.emin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoIngredientRecord {

    private String id; 
    private String ingredientString; 
    private String mealRecordId; 
}