package com.psi.appraisal.services.impl;


import com.psi.appraisal.dtos.CreateDepartmentRequest;
import com.psi.appraisal.dtos.DepartmentResponse;
import com.psi.appraisal.entity.Department;
import com.psi.appraisal.exception.DuplicateResourceException;
import com.psi.appraisal.exception.ResourceNotFoundException;
import com.psi.appraisal.repository.DepartmentRepository;
import com.psi.appraisal.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Department already exists with name: " + request.getName());
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        departmentRepository.save(department);
        return modelMapper.map(department, DepartmentResponse.class);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        return modelMapper.map(dept, DepartmentResponse.class);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(d -> modelMapper.map(d, DepartmentResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, CreateDepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        if (request.getName() != null) dept.setName(request.getName());
        if (request.getDescription() != null) dept.setDescription(request.getDescription());

        departmentRepository.save(dept);
        return modelMapper.map(dept, DepartmentResponse.class);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        departmentRepository.delete(dept);
    }
}
