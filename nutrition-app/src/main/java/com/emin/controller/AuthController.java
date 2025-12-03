package com.emin.controller;

import com.emin.dto.DtoAuthRequest;
import com.emin.dto.DtoAuthResponse;
import com.emin.dto.DtoUser;
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
        
        UserDetails userDetails = userService.loadUserByUsername(createdUser.getEmail());
        
        String jwtToken = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new DtoAuthResponse(jwtToken));
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
        return ResponseEntity.ok(new DtoAuthResponse(jwtToken));
    }
}
