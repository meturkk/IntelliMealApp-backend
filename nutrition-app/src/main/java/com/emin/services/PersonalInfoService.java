package com.emin.services;

import com.emin.dto.DtoPersonalInfo;
import com.emin.entities.PersonalInfo;
import com.emin.entities.User;
import com.emin.exceptions.ResourceNotFoundException;
import com.emin.repository.PersonalInfoRepository;
import com.emin.repository.UserRepositorty; 
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonalInfoService {

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private UserRepositorty userRepository;

    private DtoPersonalInfo convertToDto(PersonalInfo entity) {
        DtoPersonalInfo dto = new DtoPersonalInfo();
        BeanUtils.copyProperties(entity, dto);
        
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
        }
        return dto;
    }

    
    // Create
    
    
    @Transactional
    public DtoPersonalInfo createPersonalInfo(String userId, DtoPersonalInfo dtoPersonalInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        PersonalInfo personalInfo = new PersonalInfo();
        BeanUtils.copyProperties(dtoPersonalInfo, personalInfo, "id", "user", "userId"); 
        
        personalInfo.setUser(user);

        PersonalInfo savedInfo = personalInfoRepository.save(personalInfo);
        
        return convertToDto(savedInfo);
    }

    
    // Read
    
    @Transactional(readOnly = true)
    public List<DtoPersonalInfo> getAllByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        List<PersonalInfo> entities = personalInfoRepository.findByUserId(userId);

        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Update

    @Transactional
    public DtoPersonalInfo updatePersonalInfo(String infoId, DtoPersonalInfo dtoPersonalInfo) {
        PersonalInfo existingInfo = personalInfoRepository.findById(infoId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalInfo", "id", infoId));

        BeanUtils.copyProperties(dtoPersonalInfo, existingInfo, "id", "user", "userId"); 

        PersonalInfo updatedInfo = personalInfoRepository.save(existingInfo);
        return convertToDto(updatedInfo);
    }

    // Delete

    public void deletePersonalInfo(String infoId) {
        PersonalInfo info = personalInfoRepository.findById(infoId)
                .orElseThrow(() -> new ResourceNotFoundException("PersonalInfo", "id", infoId));
        
        personalInfoRepository.delete(info);
    }
}