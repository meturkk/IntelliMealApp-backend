package com.emin.services;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emin.dto.DtoPersonalInfo;
import com.emin.dto.DtoUser;
import com.emin.entities.User;
import com.emin.exceptions.ResourceNotFoundException;
import com.emin.repository.UserRepositorty;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepositorty userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    private DtoUser convertToDto(User user) {
        DtoUser dtoUser = new DtoUser();
        BeanUtils.copyProperties(user, dtoUser);
        
        if (user.getPersonalInfoRecords() != null) {
            List<DtoPersonalInfo> dtoPersonalInfoList = user.getPersonalInfoRecords().stream()
                .map(pi -> {
                    DtoPersonalInfo dtoPi = new DtoPersonalInfo();
                    BeanUtils.copyProperties(pi, dtoPi);
                    dtoPi.setUserId(user.getId());
                    return dtoPi;
                })
                .collect(Collectors.toList());
            
            dtoUser.setPersonalInfo(dtoPersonalInfoList);
        }
        return dtoUser;
    }
    
    // Read

    @Transactional
    public DtoUser getUserById(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id)); // Hata sınıfını kullan
        
        if (user.getPersonalInfoRecords() != null) {
             user.getPersonalInfoRecords().size(); 
        }
        
        return convertToDto(user);
    }

    
    // Create
    
    public DtoUser createUser(DtoUser dtoUser) {
        User user = new User();
        BeanUtils.copyProperties(dtoUser, user);
        
        // Encode password
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    // Update
    
    @Transactional
    public DtoUser updateUser(String id, DtoUser dtoUser) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
                BeanUtils.copyProperties(dtoUser, existingUser, "id", "personalInfo");
                
        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    // Delete
    
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        userRepository.delete(user);
    }
}