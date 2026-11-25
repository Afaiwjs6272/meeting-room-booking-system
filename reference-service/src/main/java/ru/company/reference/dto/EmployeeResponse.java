package ru.company.reference.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private String id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String middleName;
    private String fullName;
    private String email;
    private String phone;
    private String position;
    private DepartmentInfo department;
    private String role;
    private String managerEmployeeId;
    private Set<String> skills;
    private String telegramChatId;
    private String externalId;
    private String externalSource;
    private Boolean calendarIntegrationEnabled;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentInfo {
        private String id;
        private String code;
        private String name;
        private String managerEmployeeId;
    }
}