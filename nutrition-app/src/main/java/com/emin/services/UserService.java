package com.emin.services;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import com.emin.services.EmailService;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepositorty userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

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

    public DtoUser getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return convertToDto(user);
    }

    @Transactional(readOnly = true)
    public List<DtoUser> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    
    // Create
    
    public DtoUser createUser(DtoUser dtoUser) {
        User user = new User();
        BeanUtils.copyProperties(dtoUser, user);
        
        // Encode password
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Set default role
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        // Set default isReceived
        user.setIsReceived(0);

        // generate verification code and expiry
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        user.setVerificationCode(code);
        user.setVerified(false);
        user.setVerificationExpiry(LocalDateTime.now().plusMinutes(15));

        User savedUser = userRepository.save(user);

        // send verification email (best-effort)
        if (savedUser.getEmail() != null) {
            emailService.sendVerificationEmail(savedUser.getEmail(), code);
        }

        return convertToDto(savedUser);
    }

    public DtoUser verifyEmail(String email, String code) {
        User existingUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (existingUser.getVerificationCode() == null || !existingUser.getVerificationCode().equals(code)) {
            throw new ResourceNotFoundException("Verification", "code", code);
        }

        if (existingUser.getVerificationExpiry() != null && existingUser.getVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code expired");
        }

        existingUser.setVerified(true);
        existingUser.setVerificationCode(null);
        existingUser.setVerificationExpiry(null);

        User updated = userRepository.save(existingUser);
        return convertToDto(updated);
    }

    // Update
    
    @Transactional
    public DtoUser updateUser(String id, DtoUser dtoUser) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
            
        // Password hash check
        if(dtoUser.getPassword() != null && !dtoUser.getPassword().isEmpty()){
            existingUser.setPassword(passwordEncoder.encode(dtoUser.getPassword()));
        }
            
        BeanUtils.copyProperties(dtoUser, existingUser, "id", "personalInfo", "password");
                
        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    // Delete
    
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        userRepository.delete(user);
    }

    // Mark as received meal plan
    @Transactional
    public void markAsReceivedMealPlan(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsReceived(1);
        userRepository.save(user);
    }

    // Mark as completed meal plan
    @Transactional
    public void markAsCompletedMealPlan(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setIsReceived(2);
        userRepository.save(user);
    }
}