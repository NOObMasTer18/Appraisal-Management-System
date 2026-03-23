package com.appraisal.service;

import com.appraisal.dto.request.CreateDepartmentRequest;
import com.appraisal.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse createDepartment(CreateDepartmentRequest request);

    DepartmentResponse getDepartmentById(Long id);

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request);

    void deleteDepartment(Long id);
}
