package com.psi.appraisal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psi.appraisal.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByEmail(String email);
	
	boolean existsByEmail(String email);
	
	List<User> findByManagerId(Long managerId);
	
	List<User> findByDepartmentId(Long departmentId);
	
	List<User> findByIsActiveTrue();
}
