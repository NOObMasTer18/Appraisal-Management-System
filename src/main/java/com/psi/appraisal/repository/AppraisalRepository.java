package com.psi.appraisal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.enums.AppraisalStatus;

public interface AppraisalRepository extends JpaRepository<Appraisal, Long>{
	
    // Employee: get all my appraisals across all cycles
    List<Appraisal> findByEmployeeId(Long employeeId);
 
    // Manager: get all appraisals I need to review
    List<Appraisal> findByManagerId(Long managerId);
 
    // HR: get all appraisals for a specific cycle
    List<Appraisal> findByCycleName(String cycleName);
 
    // Prevent duplicate — one appraisal per employee per cycle
    boolean existsByCycleNameAndEmployeeId(String cycleName, Long employeeId);
 
    // Find a specific employee's appraisal in a cycle
    Optional<Appraisal> findByCycleNameAndEmployeeId(String cycleName, Long employeeId);
 
    // HR: monitor pending / overdue appraisals
    List<Appraisal> findByCycleNameAndAppraisalStatus(String cycleName, AppraisalStatus status);
 
    // Manager: see their team's appraisals in a specific cycle
    List<Appraisal> findByCycleNameAndManagerId(String cycleName, Long managerId);
	
}
