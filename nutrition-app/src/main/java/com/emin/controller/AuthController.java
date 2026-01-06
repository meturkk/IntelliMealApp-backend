package com.emin.controller;

import com.emin.dto.DtoAuthRequest;
import com.emin.dto.DtoAuthResponse;
import com.emin.dto.DtoUser;
import com.emin.dto.DtoVerifyRequest;
import com.emin.security.JwtService;
import com.emin.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<DtoAuthResponse> register(@RequestBody DtoUser request) {

        DtoUser createdUser = userService.createUser(request);
        createdUser.setPassword(null);
        
        UserDetails userDetails = userService.loadUserByUsername(createdUser.getEmail());
        
        String jwtToken = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new DtoAuthResponse(jwtToken, createdUser));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<DtoAuthResponse> authenticate(@RequestBody DtoAuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);
        
        DtoUser user = userService.getUserByEmail(request.getEmail());
        user.setPassword(null);
        
        return ResponseEntity.ok(new DtoAuthResponse(jwtToken, user));
    }

    @PostMapping("/verify")
    public ResponseEntity<DtoUser> verifyEmail(@RequestBody DtoVerifyRequest request) {
        DtoUser user = userService.verifyEmail(request.getEmail(), request.getCode());
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }
}
