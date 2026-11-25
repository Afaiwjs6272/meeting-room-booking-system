package ru.company.reference.mapper;

import ru.company.reference.dto.CreateEmployeeRequest;
import ru.company.reference.dto.UpdateEmployeeRequest;
import ru.company.reference.dto.EmployeeResponse;
import ru.company.reference.entity.Department;
import ru.company.reference.entity.Employee;
import org.springframework.stereotype.Component;
import ru.company.reference.entity.EmployeeRole;

@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeNumber(employee.getEmployeeNumber())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .middleName(employee.getMiddleName())
                .fullName(employee.getFirstName() + " " + employee.getLastName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .position(employee.getPosition())
                .department(toDepartmentInfo(employee.getDepartment()))
                .role(employee.getRole() != null ? employee.getRole().name() : "EMPLOYEE")
                .managerEmployeeId(employee.getManagerEmployeeId())
                .skills(employee.getSkills())
                .telegramChatId(employee.getTelegramChatId())
                .calendarIntegrationEnabled(employee.getCalendarIntegrationEnabled())
                .active(employee.getActive())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    public Employee toEntity(CreateEmployeeRequest request) {
        if (request == null) return null;

        Employee employee = Employee.builder()
                .employeeNumber(request.getEmployeeNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .position(request.getPosition())
                .managerEmployeeId(request.getManagerEmployeeId())
                .skills(request.getSkills())
                .telegramChatId(request.getTelegramChatId())
                .active(true)
                .build();

        if (request.getRole() != null) {
            try {
                employee.setRole(EmployeeRole.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                employee.setRole(EmployeeRole.EMPLOYEE);
            }
        } else {
            employee.setRole(EmployeeRole.EMPLOYEE);
        }

        return employee;
    }

    public void updateEntity(Employee employee, UpdateEmployeeRequest request) {
        if (request == null) {
            return;
        }

        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getMiddleName() != null) {
            employee.setMiddleName(request.getMiddleName());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition());
        }
        if (request.getManagerEmployeeId() != null) {
            employee.setManagerEmployeeId(request.getManagerEmployeeId());
        }
        if (request.getTelegramChatId() != null) {
            employee.setTelegramChatId(request.getTelegramChatId());
        }
        if (request.getSkills() != null) {
            employee.setSkills(request.getSkills());
        }
        if (request.getCalendarIntegrationEnabled() != null) {
            employee.setCalendarIntegrationEnabled(request.getCalendarIntegrationEnabled());
        }
        if (request.getActive() != null) {
            employee.setActive(request.getActive());
        }

        if (request.getRole() != null) {
            try {
                employee.setRole(EmployeeRole.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private EmployeeResponse.DepartmentInfo toDepartmentInfo(Department department) {
        if (department == null) {
            return null;
        }

        return EmployeeResponse.DepartmentInfo.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .managerEmployeeId(department.getManagerEmployeeId())
                .build();
    }
}