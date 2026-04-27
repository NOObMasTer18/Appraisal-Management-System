package com.psi.appraisal.services;

import com.psi.appraisal.config.AuthenticationResponse;
import com.psi.appraisal.dtos.auth.AuthenticationRequest;

import com.psi.appraisal.dtos.CreateUserRequest;

public interface AuthenticationService {

    AuthenticationResponse register(CreateUserRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

}
