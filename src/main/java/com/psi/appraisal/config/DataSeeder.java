package com.psi.appraisal.config;

import com.psi.appraisal.entity.Department;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.entity.enums.Role;
import com.psi.appraisal.repository.DepartmentRepository;
import com.psi.appraisal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedDepartment();
        seedHRUser();
    }

    private void seedDepartment() {
        if (!departmentRepository.existsByName("HR")) {
            Department hrDept = Department.builder()
                    .name("HR")
                    .description("Human Resources Department")
                    .build();
            departmentRepository.save(hrDept);
            System.out.println("Seeded HR Department");
        }
    }

    private void seedHRUser() {
        String hrEmail = "hr@psi.com";
        if (!userRepository.existsByEmail(hrEmail)) {
            Optional<Department> hrDept = departmentRepository.findByName("HR");
            
            User hrUser = User.builder()
                    .fullName("HR Admin")
                    .email(hrEmail)
                    .password(passwordEncoder.encode("hr123"))
                    .role(Role.HR)
                    .jobTitle("HR Manager")
                    .department(hrDept.orElse(null))
                    .isActive(true)
                    .build();
            userRepository.save(hrUser);
            System.out.println("Seeded HR Admin User: " + hrEmail);
        }
    }
}
