package com.psi.appraisal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.psi.appraisal.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
 
    Optional<Department> findByName(String name);
 
    boolean existsByName(String name);
}
 
