package ru.company.reference.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

    @NotBlank(message = "Employee number is required")
    @Size(min = 3, max = 50, message = "Employee number must be between 3 and 50 characters")
    private String employeeNumber;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String middleName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Position is required")
    private String position;

    private String departmentId;

    private String role;

    private String managerEmployeeId;

    private String externalId;

    private String externalSource;

    private String telegramChatId;

    private Set<String> skills;

    private Boolean calendarIntegrationEnabled;
}