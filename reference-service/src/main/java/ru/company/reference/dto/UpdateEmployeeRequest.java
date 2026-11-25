package ru.company.reference.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    private String id;

    private String firstName;

    private String lastName;

    private String middleName;

    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String position;

    private String departmentId;

    private String role;

    private String managerEmployeeId;

    private String externalId;

    private String telegramChatId;

    private Set<String> skills;

    private Boolean calendarIntegrationEnabled;

    private Boolean active;
}