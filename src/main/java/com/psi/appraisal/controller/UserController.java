package com.psi.appraisal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.psi.appraisal.dtos.ApiResponse;
import com.psi.appraisal.dtos.UserResponse;
import com.psi.appraisal.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	// Any: get own profile
	// GET /api/users/me?userId=1
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> getMe(@RequestParam Long userId) {

		UserResponse response = userService.getMe(userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// Manager/HR: get any user by ID
	// GET /api/users/{id}
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {

		UserResponse response = userService.getUserById(id);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	// Manager/HR: get all direct reports under a manager
	// GET /api/users/manager/{managerId}/team
	@GetMapping("/manager/{managerId}/team")
	public ResponseEntity<ApiResponse<List<UserResponse>>> getTeam(@PathVariable Long managerId) {

		List<UserResponse> responses = userService.getTeamByManager(managerId);
		return ResponseEntity.ok(ApiResponse.success(responses));
	}

}
