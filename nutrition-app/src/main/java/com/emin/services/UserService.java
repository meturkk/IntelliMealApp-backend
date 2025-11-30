package com.emin.services;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.emin.dto.DtoPersonalInfo;
import com.emin.dto.DtoUser;
import com.emin.entities.PersonalInfo;
import com.emin.entities.User;
import com.emin.repository.UserRepositorty;

@Service
public class UserService {

    @Autowired
    private UserRepositorty userRepository;

    public DtoUser getUserById(String id) {
        
        DtoUser dtoUser = new DtoUser();
        DtoPersonalInfo dtoPersonalInfo = new DtoPersonalInfo();

        Optional<User> optional = userRepository.findById(id);
        if (optional.isEmpty()) {
            return null; // Kullanıcı bulunamadı
        }
        User user = optional.get();
        PersonalInfo personalInfo = optional.get().getPersonalInfo();   

        BeanUtils.copyProperties(user, dtoUser);
        BeanUtils.copyProperties(personalInfo, dtoPersonalInfo);

        dtoUser.setPersonalInfo(dtoPersonalInfo);

        return dtoUser;
    }
}
